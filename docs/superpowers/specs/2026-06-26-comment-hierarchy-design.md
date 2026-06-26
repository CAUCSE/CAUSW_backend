# Comment Hierarchy Design

## Goal

Unify the current `Comment -> ChildComment` model into a single hierarchical `Comment` entity while preserving the existing one-level reply behavior. The first implementation supports root comments and direct replies only. Deeper trees remain blocked by service validation, so the database structure can support a future expansion without another major table migration.

## Current State

The community comment domain currently has separate entities and tables:

- `Comment` uses `tb_comment` for root comments.
- `ChildComment` uses `tb_child_comment` for replies.
- Likes are split between `tb_like_comment` and `tb_like_child_comment`.
- APIs are split between `/api/v2/comments` and `/api/v2/child-comments`.
- Report, block, notification, post comment counts, and profile rendering all know about `ChildComment`.

This duplication makes every reply-related change require parallel implementation.

## Target Model

`Comment` becomes the only comment entity.

- Root comment: `parentComment = null`
- Reply comment: `parentComment != null`
- Reply depth: exactly one level for now
- `childCommentList`: self-referencing children mapped by `parentComment`
- `post`: retained on every row, including replies, for efficient lookup and count queries

The service layer rejects creating a reply under another reply. This keeps current API behavior intact while leaving the schema ready for deeper trees later.

## API Compatibility

The initial rollout keeps the existing public response shape.

- `/api/v2/comments` continues to list root comments with `childCommentList`.
- `/api/v2/child-comments` remains available for compatibility, but internally uses `Comment`.
- New internal command flow accepts `parentCommentId` as nullable.
- Response field names such as `numChildComment`, `childCommentList`, and `isChildCommentLike` may remain during the compatibility phase.

After frontend migration, a follow-up cleanup can rename DTO fields and remove child-comment-specific endpoints.

## Data Migration

Flyway migration performs a forward-only conversion:

1. Add nullable `parent_comment_id` to `tb_comment`.
2. Copy every row from `tb_child_comment` into `tb_comment`.
   - Preserve `id`, `content`, `is_deleted`, `is_anonymous`, `user_id`, `created_at`, and `updated_at`.
   - Set `parent_comment_id` from `tb_child_comment.parent_comment_id`.
   - Set `post_id` from the parent comment's `post_id`.
3. Copy `tb_like_child_comment` rows into `tb_like_comment`.
4. Add a self-referencing foreign key and useful indexes.

The first implementation should avoid dropping legacy tables until the code path is stable. Removing `tb_child_comment` and `tb_like_child_comment` can be a separate cleanup migration.

## Domain Changes

`ChildComment` and `LikeChildComment` are removed from active domain logic. Existing readers, writers, validators, and mappers are either deleted or converted into compatibility wrappers around `CommentService`.

`CommentRepository` owns all root and reply queries:

- Fetch root comments by post with `parentComment IS NULL`.
- Fetch replies by parent IDs.
- Count non-deleted replies by parent or post.
- Find posts commented on by a user, including replies.

`CommentReader` batches replies into each root comment to preserve the current list API shape.

## Business Rules

- A root comment requires a post ID and no parent ID.
- A reply requires a valid parent comment ID.
- A reply inherits the parent comment's post.
- A reply cannot be created under another reply.
- Soft-deleted comments stay in place and return `content = null`.
- Like, report, block, and notification behavior should match the current root-comment and child-comment behavior.

## Integration Points

Post comment counts must count all non-deleted rows in `tb_comment`, including replies.

Notification events should still distinguish:

- New root comment on a post
- New reply under a comment

Report and block compatibility may keep `CHILD_COMMENT` names externally at first, but their readers should resolve targets from `Comment`.

## Testing

Tests should cover:

- Creating a root comment.
- Creating a reply under a root comment.
- Rejecting a reply under another reply.
- Listing comments with replies attached.
- Counting root comments and replies from the unified table.
- Liking and unliking a reply through the compatibility endpoint.
- Reporting/blocking reply targets after `ChildComment` removal.
- Notification behavior for root comments and replies.

Run focused comment-domain tests first, then `:app-main:spotlessCheck` and `:app-main:compileJava`. If migration SQL changes, run Flyway validation if the local environment supports it.

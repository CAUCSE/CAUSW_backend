# Contributing

이 Repository는 중앙대학교 소프트웨어학부 동문네트워크 프로젝트의 Backend Repository입니다.
Contribute 하려는 Repo가 해당 Repo가 맞는지 확인을 꼭 해주시기 바랍니다.

## 개발 순서
### 1. Fork and Clone
모든 contribution은 fork한 레포를 통해 진행합니다.
그러므로 `CAUCSE/CAUSW_backend` 레포에서 개인 레포로 fork 해줍니다.

그리고 개인 repo를 로컬 저장소로 clone 해줍니다.
```shell
git clone https://www.github.com/{개인_계정}/CAUSW_backend
```
그 후 upstream branch로 BE Repo를 추가해줍니다.
```shell
git remote add upstream https://github.com/CAUCSE/CAUSW_backend.git
```

### 2. Commit
// TODO - 개발 시 환경변수 등 세팅에 대한 내용 추가

개발이 완료 된 후에, Commit을 남겨주며, Commit에 대한 규칙은 다음과 같습니다.
#### - commit 규칙
```
(커밋 타입): (커밋 타이틀)
```
#### - commit 타입
```
feat: 신규 기능 혹은 기능 추가 개발 시
fix: 기능 수정 및 버그 수정 시
refactor: 코드 성능 개선
chore: 환경변수나 기타 단순 수정
typo: 오탈자 수정
lint: 코드 스타일 수정
```

### 3. Pull Request
PR을 올리기 이전에, upstream branch에 변경 사항이 있을 수 있으므로 해당 내용들을 `fetch`, `merge`해줍니다.
```shell
git fetch upstream
git merge upstream/develop
```

그 후, `CAUCSE/CAUSW_backend` 레포에 Pull Request를 PR Template에 맞게 작성 후 올립니다.
Merge는 1인 이상의 review를 받아야합니다.
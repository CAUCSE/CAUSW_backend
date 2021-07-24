# Contributing

이 Repository는 중앙대학교 소프트웨어학부 동문네트워크 프로젝트의 Backend Repository입니다.
Contribute 하려는 Repo가 해당 Repo가 맞는지 확인을 꼭 해주시기 바랍니다.

## 1. Fork and Clone
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
## 2. Make new branch
```shell
git checkout -b (브랜치명)
```
develop 브랜치에서, 개별 개발용 branch를 생성합니다.<br/>
branch명에 대한 규칙은 별도로 없으나, `(커밋 타입)/(개발 내용)`의 형태를 따르는 것을 추천드립니다.
커밋 타입은 하단 *3번 항목*을 참조 부탁드립니다.


## 3. Develop and Commit
개발이 완료 된 후에, Commit을 남겨주며, Commit에 대한 규칙은 다음과 같습니다.
#### - commit 규칙
```
(커밋 타입): (커밋 타이틀)
```
#### - commit 타입
```
feat: 신규 기능 혹은 기능 추가 개발 시
fix: 기능 수정 및 버그 수정 시
refac: 코드 성능 개선
docs: 문서 관련 수정
chore: 환경변수나 기타 단순 수정
typo: 오탈자 수정
lint: 코드 스타일 수정
```

## 4. Pull Request
PR을 올렸을 때, conflict가 발생한다면, 다음과 같은 절차를 진행해주시기 바랍니다.

upstream branch에 변경 사항이 있을 경우 conflict가 생길 수 있으며,
해당 내용들을 `fetch`, `rebase`해줍니다.
```shell
git fetch upstream
git rebase upstream/develop
```

`CAUCSE/CAUSW_backend` 레포에 Pull Request를 PR Template에 맞게 작성 후 올립니다.
Merge는 1인 이상의 review를 받아야합니다.

## 5. Cleanup branch
개발한 브랜치를 정리해줍니다.<br/>
우리 repository는 squash merge를 하기때문에, 해당 브랜치에서 개발을 진행하는 경우 conflict가 발생할 수 있기 때문입니다.

```shell
git checkout develop
git branch -d (브랜치명)
git fetch upstream
git rebase upstream/develop
```
## 🖥️ 프로젝트 소개
<img width="91" height="96" alt="Image" src="https://github.com/user-attachments/assets/2f02b6dd-38c3-4d00-965f-28d17d4f77a3" />
- 실사용자 수: 약 700명
- 주요 타겟: 중앙대학교 소프트웨어대학 동문(크자회 회원)
- 핵심 기능: 동문 수첩, 게시판, 학부 공지, 경조사 서비스
- 서비스 링크: https://causw.co.kr


### ⚙️ 개발 환경
- **Language** : Java 17
- **Framework** : SpringBoot 3.2.0
- **Database** : AWS RDS (MySQL 8.4.7)
- **ORM** :  Spring Data JPA (QueryDSL)
- **Build**: Gradle 

## 🖥️ 서버 환경
<img width="1325" height="823" alt="Image" src="https://github.com/user-attachments/assets/4e182e9d-4c1a-496c-8678-b3a99b394ca5" />

## 📌 주요 기능
- 동문 수첩: 이용자의 프로필을 등록하고 회원들과 공유할 수 있는 동문 수첩 서비스를 운영합니다.<br>
- 게시판: 회원들이 자유롭게 등록하고, 댓글을 달 수 있는 자유 게시판과 학생회 및 크자회의 공지를 등록하는 게시판등을 운영합니다.<br>
- 학부 공지: 학부 홈페이지에 등록되는 공지들을 모아서 등록 및 공지해주는 서비스를 운영합니다.<br>
- 경조사 서비스: 경조사를 등록하고 원하는 그룹에게 공유하는 서비스를 운영합니다.<br>

## 🏗️ 시스템 아키텍처 
- app-main/

 └─ src/main/java/net.causw.app.main
    ├─ controller/        
    ├─ service/           
    ├─ repository/        
    ├─ domain/  
    │   ├─ model/         
    │   ├─ policy/        
    │   └─ validation/    
    ├─ dto/               
    ├─ infrastructure/    
    └─ CauswApplication
  
  -global/

 └─ src/main/java/net.causw.global
    ├─ constant/     
    ├─ exception/    
    └─ util/         

### 대표 도메인
- User & Academic 회원, 학적 정보 관리

- Circle & Community 동아리, 게시판, 게시글, 댓글/대댓글 등 커뮤니티 기능

- Event & Schedule 학사 일정, 행사, 학기/학사 관련 스케줄 관리

- Application & Form 폼 처리

- Facility & Resource

- Notification 푸시/알림 발송 및 로그 관리

- Common / Global 공통 API, 전역 예외 처리 등 공용 기능


## 📂 디렉터리 구조 

## Contact
Email : caucsedongne@gmail.com

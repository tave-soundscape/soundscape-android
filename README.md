## 📌 프로젝트 소개

<img width="1920" height="1080" alt="DIP_표지" src="https://github.com/user-attachments/assets/79dcf6fa-a081-4527-b1cf-78f2f1fb8c06" />


단순히 장르별로 묶인 플레이리스트에 질리셨나요? **DIP**은 지금 당신이 위치한 공간의 '맥락'을 읽습니다.

1. 위치를 읽다: 당신이 머무는 공간의 특성을 분석하여 장소의 감성을 극대화합니다.
2. 소음을 분석하다: 다음으로 당신 주변의 데시벨을 측정합니다. 소음을 뚫고 들릴 강력한 비트 혹은 고요함을 채워줄 선율을 골라냅니다.
3. 목표에 집중하다: 당신이 설정한 오늘의 목표에 맞춰 음악이 단순한 감상을 넘어 당신의 활동을 돕는 도구가 됩니다.

지금 바로 당신의 환경이 들려주는 음악에 귀를 기울여 보세요.


## ✨ 주요 기능 

- **맥락 기반 추천**: 사용자의 위치, 주변 소음, 목표를 기반으로 Agent가 사용자 맞춤형 플레이리스트 생성
- **실시간 분석**: 실시간 주변 소음 데시벨 측정 및 현재 위치와 목표 설정 
- **Spotify 연동**: 생성된 플레이리스트를 Spotify 앱으로 바로 연결 (Deep Link)
- **히스토리 관리**: Room DB를 활용하여 최근 추천받은 몰입 테마 기록 저장 및 관리
- **소셜 로그인**: Kakao OAuth를 이용한 간편 로그인 및 사용자 인증

<br/>

## 🎨 UI

|소셜 로그인|홈화면|홈 히스토리|
|---|---|---|
|<img width="120" alt="kakao_oauth_screenshot" src="https://github.com/user-attachments/assets/b4da05ba-b9bd-4c79-98f8-836cbcfbd08e" />|<img width="120" alt="home_screenshot" src="https://github.com/user-attachments/assets/0d2b0162-bf8d-40ab-b15c-96fe6d070e6e" />|<img width="120" alt="home_history_screenshot" src="https://github.com/user-attachments/assets/8f7f79b4-7e2e-4021-93c8-02841e9995be" />|

|라이브러리|둘러보기|마이페이지|
|---|---|---|
|<img width="1200" height="2541" alt="library_screenshot" src="https://github.com/user-attachments/assets/79c32ce5-c71f-4a04-8f1c-9dcaf01d38ba" />|<img width="1200" height="2541" alt="explore_screenshot" src="https://github.com/user-attachments/assets/358d75b9-0bb9-4d90-b735-34f806ffd926" />|<img width="1200" height="2541" alt="mypage_screenshot" src="https://github.com/user-attachments/assets/29d79fb3-3ef0-46b7-9b9e-0c4dbf1731b8" />|

<br/>

## 📱 실행 화면 

| 온보딩 | 추천 받기 |
|---|---|
|<img src="https://github.com/user-attachments/assets/500f6079-8c37-428e-8069-7ab2987bedc9" width="320" />|<img src="https://github.com/user-attachments/assets/15bf8aa5-ed76-4be7-a292-4c64e36a4ab5" width="320"/>|

| 라이브러리 | 둘러보기 |
|---|---|
|<img src="https://github.com/user-attachments/assets/3350eff3-9bf9-49e6-be80-18b31127aa21" width="320"/>|<img src="https://github.com/user-attachments/assets/37769da1-5a81-4baf-82f8-afcb2b589fe0" width="320"/>|

<br/>

## 🧑‍💻 프론트엔드 팀원 소개

|서정현|최수정|
|:------:|:------:|
| <img src="https://avatars.githubusercontent.com/u/200588020?v=4" alt="서정현" width="150"> | <img src="https://avatars.githubusercontent.com/u/165886570?v=4" alt="최수정" width="150"> |
| **FE** | **FE** |

<br/>

## 🛠 프론트엔드 기술 스택

|Category|Technology|
|--|--|
|Platform|Android|
|Language|Kotlin|
|UI|XML|
|Architecture|MVVM|
|Network|Retrofit2|
|Image Loading|Glide|
|Local DB|Room Database|
|Async|Coroutines|
|Authentication|Kakao OAuth|
|Build Tool|Gradle, Git|

<br/>

## 📂 프로젝트 구조

```
com.mobile.soundscape
├── api
│   ├── apis/            # Retrofit API 인터페이스 정의
│   ├── client/          # Retrofit, OkHttp 클라이언트 설정
│   └── dto/             # 서버 통신용 DTO
│
├── data/                      # Repository/Token 관리 Manager,  공용 데이터 정의, 로컬 DB 
├── home/                    # 홈, 라이브러리, 마이페이지  
├── explore/                  # 둘러보기 화면
├── recommendation/     # 장소, 데시벨, 목표 설정 화면 
├── result/                    # 추천 결과 화면
├── evaluation/              # 사용자 평가 화면
├── onboarding/            # 온보딩(이름 설정, 아티스트 및 장르 취향 설정)
├── login/                     # 로그인
│
├── MainActivity.kt             # 메인 Activity
└── SoundscapeApp.kt       # Application 클래스
```



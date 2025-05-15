# 🚌 RefacSchoolBusReservationForUser-kotlin

학교 스쿨버스 예약 앱 (사용자용) Kotlin 리팩토링 버전입니다.  
기존 Java 버전의 구조를 개선하고, 사용자 경험(UX)과 인터페이스(UI)을 향상시킨 프로젝트입니다.

---

## ✨ 주요 기능

### 1. 🔐 로그인 / 회원가입 화면 (Login.kt, Register.kt)
-Firebase Authentication을 이용한 이메일/비밀번호 로그인
-자동 로그인 기능(CheckBox 활용) => sharedPreference
-회원가입 후 Firebase에 사용자 정보 저장

### 2.🏠 메인 화면 (RouteChoose.kt)
- 4개의 등교노선과 1개의 하교선택 버튼으로 구성
- 클릭 시 선택한 노선에 따라 TimePlace 페이지 내 구성요소 변경 (TextView, Spinner 내 시간과 정류장, 지도 화면)

### 3. ⏰ 시간 및 정류장 선택 화면 (TimePlace.kt)
- 선택한 노선에 따라 Spinner 동적으로 갱신
- 예약 가능한 시간만 필터링하여 표시 (현재 시간 기준 이후 시간만)
- 정류장 선택 (Firebase에 정류장 정보 저장)
- ProgressBar로 로딩 상태 표시
- 예약 완료 시 Firebase Firestore에 저장
- 예약 중복 제한(등교/하교 각각 하루에 1건만 가능하도록)

### 4. 📋 예약 확인 화면 (SelectBusList.kt)
- 사용자가 예약한 내역을 리스트로 조회
- 예약이 없을 경우 안내 메시지 표시
- 각 예약 항목에 대해 삭제 버튼 제공
- 삭제 시 실시간으로 Firebase에서 제거
- 예약 후 버스 탑승 시간 10분 전 알람 이벤트 기능(아직 테스트 미완료)

### 5. 🧩 공통 기능 (FAB 및 설정 등)
- Floating Action Button으로 아래 기능 접근 가능:
  - ✅ 학교 홈페이지의 스쿨버스 노선 페이지로 이동
  - ✅ 예약 확인 화면 바로가기
  - ✅ 로그아웃
  - ✅ 다크모드 전환 토글 (사용자 기기에 따라 테마 적용)

---

## ⚙️ 사용 기술

| 분류 | 기술 |
|------|------|
| 언어 | Kotlin |
| IDE  | Android Studio |
| DB   | Firebase Firestore |
| 인증 | Firebase Authentication |
| 구조 | MVVM 일부 적용 (단계적 도입 중) |
| 기타 | RecyclerView, Spinner, ViewBinding 등 |

---

## 🔄 리팩토링 포인트

- Java → Kotlin 전환
- 여러 XML 분리 구조 → 하나의 동적 레이아웃 구조
- UX 개선: 하루에 하나의 예약으로 제한(), Spinner 필터링
- UI 개선: 다크모드 대응, ProgressBar

---

> 📌 스크린샷 및 시연 영상은 추후 업데이트 예정입니다!

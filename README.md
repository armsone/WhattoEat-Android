# 오늘 뭐 먹지?? — Android

현재 iOS `0.3.0` (`202608241540`, Git `730e95a`) 소스와 실제 simulator 캡처를 literal reference로 관리하는
Android 네이티브 앱. 구형 handoff와 이전 캡처는 역사 자료이며 최신 iOS 소스와 충돌할 때 정본이 아니다.
앱 소유 UI/동작은 Material 기본 스타일로 재해석하지 않는다.

## 요구 사항

- Kotlin 2.1 + Jetpack Compose (AndroidX BOM), 외부 런타임 의존성 없음
- Gradle 9.5.0 (wrapper 포함), AGP 9.3.0, compileSdk/targetSdk 37, minSdk 26
- `applicationId` / `namespace`: `com.nasfinder.whattoeat`
- versionName `0.3.6`, versionCode `339619`, `BuildConfig.BUILD_STAMP = "202608242019"`

## 빌드

```bash
./gradlew assembleDebug
```

Debug/Release 빌드 모두 `preBuild`가 `copyHandoffAssets` 태스크로 `android-handoff/source-assets/**`의
원본 PNG를 `app/src/main/res/drawable-nodpi/`로 손실 없이 복사한다. `android-handoff/` 자체는
읽기 전용 원본이며 수정하지 않는다.

## 테스트

```bash
./gradlew testDebugUnitTest        # MenuPolicy / RecommendationPool / ChoiceStore(Robolectric)
./gradlew connectedDebugAndroidTest # Compose UI 테스트 (AppBottomBar 등, 기기/에뮬레이터 필요)
```

## 매치업 카탈로그 재현

이미 설치된 debug 빌드와 기존 ADB 대상이 있을 때만 새 빈 폴더로 캡처한다. 이 명령은 앱을 설치하거나
에뮬레이터를 시작하지 않으며 기존 폴더를 덮어쓰지 않는다.

```bash
scripts/capture-matchup-catalog.sh --serial <ADB_SERIAL> --output <NEW_EMPTY_DIRECTORY>
```

현재 iOS 0.3.0의 새 12상태 simulator 원본과 SHA-256/프로필은
`docs/ios-current-20260824-1737/`, 재감사 범위와 미검증 상태는 `docs/PARITY_MATRIX.md`에 있다.
이전 iOS/Android 카탈로그는 비교 이력일 뿐 현재 parity 통과 증거가 아니다.

## 설치

```bash
./gradlew installDebug
```

## 아키텍처

- `data/` — `ChoiceStore`(SharedPreferences JSON 영속화), `ApiClient`(HTTPS `/api/restaurants`),
  `LocationService`(FusedLocation 대신 플랫폼 `LocationManager` + `Geocoder`), `ImageLoader`
  (iOS direct 사진 계약 + HTTPS 사진 로드 + fallback 매칭 + memory/HTTP cache), `MapProviderHelper`(네이버/카카오/구글 intent),
  `NotificationHelper` + `receiver/`(점심 알림 `AlarmManager` 반복 예약).
- `model/Models.kt` — iOS `Models.swift`와 1:1 대응하는 데이터 모델·enum.
- `viewmodel/MainViewModel.kt` — 전역 내비게이션/추천/설정 상태를 보관하는 단일 `AndroidViewModel`.
  `ContentView.swift`의 상태 전이(`AppPage`, `RecommendationPhase`, `pageBeforeSettings` 등)를 그대로 옮겼다.
- `ui/components/` — 앱 소유 chrome(카드, 버튼, 하단바, 아이콘 Canvas, 다이얼로그, 이미지 fallback).
  Material 기본 `AlertDialog`/`Switch`/`Card` 대신 직접 그린 컴포넌트만 사용한다.
- `ui/screens/` — 화면별 Compose 함수 (Home/Region/Result/Decision/History/Favorites/Settings).
- `ui/RootApp.kt` — 하단바 고정 + 600dp 중앙 rail + 전역 다이얼로그 오버레이.
- 하단바는 대표님의 최신 Android 전용 직접 지시에 따라 current iOS-derived anatomy 전체를 1.5배로 적용하며, 좌우·하단 여백은 각각 28dp다. SM-F968N 실제 화면 후속 지시로 중앙 원 diameter만 57.834dp, group lift -15.897dp로 조정했고 36dp 주사위는 그대로 유지했다. 선택된 `추천`은 다른 tab과 같은 진한 흰 label+빨간 indicator를 쓴다. 이 값은 일반 Android 예외나 시각 일치 판정이 아니다.
- adaptive launcher icon은 108dp background, 약 60.1dp visible lunchbox foreground, 60dp monochrome safe-zone을 분리해 사용한다.
- 폴더블 등 physical display가 여러 개인 기기에서는 캡처 스크립트가 현재 active physical display ID를 명시하고 manifest에 기록하며, 앱이 잠금 화면 뒤에 있으면 캡처를 중단한다.
- 외부 사진 provider 선택/검색은 앱이 복제하지 않고 iOS와 같은 `https://nasfinder.com/api/restaurants` 서버 응답을 exact key로 decode한다. `photoURL`과 사진 메타데이터를 결과→결정→최근/찜까지 유지하며, nil 사진은 0.9초·1.8초 재조회에서 id별로 보강한다. 현재 12상태 fixture는 remote 사진이 아니라 결정론적 local fallback 검증이다.

## OS 소유 예외 (Material/Android 기본을 써도 되는 유일한 영역)

`android-handoff/parity-manifest.json`의 `parityPolicy.osOwnedExceptions`를 따른다:

- 상태바 / 내비게이션 바
- 시스템 키보드/IME
- 시스템 위치·알림 권한 프롬프트
- 외부 지도 앱 (네이버/카카오/구글 intent로 열리는 화면 내부)
- Play 스토어 화면
- 지도 provider tile 내부(프레임/오버레이/marker/탭 동작은 앱 소유)
- 점심 알림 시각 선택에 사용하는 시스템 `TimePickerDialog` (iOS도 system time picker를 사용)

이 외의 모든 카드/버튼/다이얼로그/토글/아이콘/애니메이션은 앱 소유이며 `TECHNICAL_SPEC.md`의
색상 토큰(`theme/Color.kt`), typography(`theme/Type.kt`), 정확한 한국어 문구를 그대로 사용한다.

## 알려진 제한

`docs/PARITY_MATRIX.md`에 상세 기록. 요약:

- 최근/찜 좌측 swipe reveal은 소스에 구현했지만 이번 변경 뒤 기기 동작·화면은 미검증이다.
- 추천 보조 목록은 현재 iOS 0.3.0 소스의 2열 grid를 사용한다.
- Android 내부 지도는 OpenStreetMap tile을 사용하므로 Apple MapKit tile 자체는 같지 않다. frame과 app overlay는 별도 매치업 대상이다.
- SM-F968N에는 데이터 유지 교체 설치와 cold launch까지 확인했지만, 화면 잠금 인증 때문에 Android post-change paired capture는 아직 만들지 못했다.
- tablet 600dp+/landscape, Google TV D-pad focus, 큰 글자/TalkBack, 지도 오류, persistence relaunch는 후속 runtime 증거가 필요하다.
- 현재 상태는 `implemented from current iOS source; Android visual parity unverified`이며 완료 주장은 하지 않는다.

# iPhone ↔ Android current-source 매치업 감사

결론: **implemented from current iOS source; Android visual parity unverified**.

대표님의 실제 화면 확인에 따라 이전 12쌍의 모든 일치 판정과 근거 없는 Android 확대 예외는 폐기했다. 이후 대표님의 최신 Android 전용 직접 지시로 하단바 전체 anatomy를 current iOS-derived 값의 정확히 1.5배로 적용했다. 이는 일반 플랫폼 예외나 일치 판정이 아니다.

## 현재 literal reference

| 항목 | 확인값 |
|---|---|
| iOS 저장소 | `/Users/armsone/git/WhattoEat ` |
| iOS Git | `730e95a5ac5293fa6a6c659ec6facf0aed912df8`, clean |
| iOS 제품 | `0.3.0 (202608241540)` |
| iOS 소스 | `ContentView.swift`, `Theme.swift`, `Assets.xcassets` |
| 새 실제 카탈로그 | `docs/ios-current-20260824-1737/matchup/*.png` |
| 캡처 프로필 | iPhone 17 Pro, iOS 26.5, 402×874pt / 1206×2622px, ko-KR, light |
| 해시·메타데이터 | `docs/ios-current-20260824-1737/capture-manifest.tsv` |
| Android 실제 카탈로그 | `docs/android-current-20260824-1936/` — SM-F968N/API 36, 12 fixture + launcher + live remote + photo sheet |

새 카탈로그는 단일 simulator destination과 단일 build worker로 만들었다. 각 상태 전에 simulator의 앱 컨테이너를 다시 설치해 history/favorites fixture가 이전 상태의 저장 데이터에 오염되지 않게 했다. iOS 저장소 소스는 수정하지 않았다.

`android-handoff/**`, `docs/ios-current-20260824/**`, `docs/android-current-20260824/**`는 역사 자료다. 최신 iOS 0.3.0 소스나 위 새 캡처와 충돌할 때 정본이 아니다. 기존 PNG는 덮어쓰지 않았다.

## 분류

- **Visual:** 앱이 그리는 geometry, typography, bitmap, color, stroke, stitch, shadow, animation frame.
- **Content/state:** fixture 데이터, 선택 상태, 날짜, 문구, empty/populated/recorded 상태.
- **Functional:** 탭 전이, gesture, 저장, 외부 앱 분기, 접근성, keyboard, D-pad.
- **Forced OS exception:** status bar, Dynamic Island/home indicator/gesture 영역, system keyboard/IME, system permission UI, 외부 앱 내부 화면, system time picker만 해당한다.

## 전체 route/state inventory

아래 `구현 근거`는 source-level 대응이며 시각 통과 판정이 아니다. 새 Android paired capture가 없으므로 모든 행의 최종 상태는 미검증이다.

| Route/state | 최신 iOS literal anatomy/state | Android 구현 근거 | 분류 | 최종 상태 |
|---|---|---|---|---|
| `global.chrome` | mintBase surface, 600pt rail, 72pt content bottom reserve | `RootApp.kt`: mint surface, 600dp rail, Android override bar의 126dp 높이+28dp 하단 여백을 비우는 154dp reserve | Visual | Android visual parity unverified |
| `global.header` | 64pt row, 28pt outer inset, 34pt well, 20pt bold title, gear/close | `CompactHeader` | Visual/Functional | Android visual parity unverified |
| `global.bottomBar` | iOS literal: 84pt raised-center leather surface, 28pt side/12pt bottom, tiled texture, gradient, strokes, double stitch, shadow | Android 직접 지시: 좌우 28dp 유지, bottom 28dp, 높이·shape·texture tile·stroke·dash·shadow·내부 padding을 iOS-derived 값의 1.5배 | Visual | Android direct override implemented; actual capture comparison required |
| `global.bottomTabs` | iOS literal: 5등분, 20pt icon, 10pt label, red 14×2.5 indicator | Android 직접 지시: 30dp icon, 15sp/18sp label, 21×3.75dp indicator와 모든 spacing 1.5배; `map.fill`/`clock.arrow.circlepath` silhouette 재제작 | Visual/Functional/Accessibility | Android actual capture comparison required; instrumentation not run |
| `global.recommendTab` | iOS literal: 45.36pt circle, 24pt die, 1.2pt rim, -14pt lift | Android 직접 지시: bar의 1.5배 geometry는 유지하되 실제 SM-F968N 캡처 뒤 circle만 57.834dp로 축소, 36dp die는 그대로 보존, bottom optical edge 유지를 위해 group lift -15.897dp; 선택 state는 다른 tab과 같은 진한 흰 label+빨간 indicator | Visual | Android 새 capture comparison required |
| `home` | bitmap wordmark 162×41 + rounded 36pt black `??`, -12pt overlap, -1.5 tracking, +1pt baseline; hero before question; 58pt pin; two 76pt location cards inset to hero stitch rail | `HomeScreen.kt`, `WordmarkView`, copied source assets | Visual/Functional | implemented from current iOS source; Android visual parity unverified |
| `region` | compact header; two-part location mode; status; search; nearby/frequent cards | `RegionScreen.kt` | Visual/Content/Functional | Android visual parity unverified |
| `region-search` | same route with focused field and system keyboard; app bottom bar remains behind IME | debug `region-search`, `SOFT_INPUT_ADJUST_NOTHING` | Visual/Functional/Forced OS | app UI and keyboard interaction unverified; keyboard pixels excluded only inside OS-owned keyboard bounds |
| `loading` | compact result header, deterministic matchup candidates, shuffle scene, 10-second retry reveal | `ResultScreen.kt`, fixed debug frame/fixture | Visual/Content/Functional | Android animation and retry transition unverified |
| `results` | result header/region, main restaurant card, 2-column `LazyVGrid`, heart/phone/detail actions | `ResultScreen.kt` two-column grid and phone/heart/detail actions | Visual/Content/Functional/Accessibility | Android visual/action parity unverified |
| `decision` | close header, hero, optional phone row, app-owned map frame/overlays, map CTA, record CTA, business information action | `DecisionScreen.kt`, `PhoneDialer.kt` | Visual/Content/Functional/Accessibility | Android visual/action parity unverified; provider tile content differs |
| `decision-recorded` | same route with record persistence state generated from clean fixture | debug fixture + `ChoiceStore` | Content/Functional | Android paired state and relaunch persistence unverified |
| `history-empty` | 최근 한 끼 header/region + source empty illustration/copy | `HistoryScreen.kt`, identical source bitmap | Visual/Content | Android visual parity unverified |
| `history-populated` | image/menu/name/full Korean date + swipe/delete semantics | `HistoryScreen.kt`, `SwipeRevealRow`, fixed KST fixture | Visual/Content/Functional/Accessibility | Android visual, swipe, deletion and persistence unverified |
| `favorites-empty` | 찜한 맛집 header/region + source empty illustration/copy | `FavoritesScreen.kt`, identical source bitmap | Visual/Content | Android visual parity unverified |
| `favorites-populated` | image/name/category/full Korean date/filled heart + swipe/unfavorite semantics | `FavoritesScreen.kt`, `SwipeRevealRow`, fixed KST fixture | Visual/Content/Functional/Accessibility | Android visual, swipe, action and persistence unverified |
| `settings` | close header; permission card; Apple·네이버·카카오·Google selector; reminder; copyright disclosure | `SettingsScreen.kt` | Visual/Content/Functional | Android visual/action parity unverified |

## source-only variants, modals and interactions

12장 기본 카탈로그 밖의 상태도 완료로 간주하지 않는다.

| State/interaction | iOS source surface | Android source surface | 상태 |
|---|---|---|---|
| manual/denied/locating/searching/failed/empty result | `Phase` branches and `StatusView` | `RecommendationPhase` branches | source inventory 완료; paired runtime 미검증 |
| loading retry ≥10s | delayed retry button | delayed retry button | timer/animation runtime 미검증 |
| remote/fallback restaurant photo | HTTPS remote overlay over deterministic local fallback; no loading/error UI | direct `photoURL` contract, 15s HTTPS loader, 180ms fade, Crop, no status UI | fixed JSON/unit evidence only; live remote rendering 시각 미검증 |
| photo information sheet | `PhotoInformationSheet` | `PhotoInfoSheet`: loaded remote with metadata only, bottom-leading 26dp info circle, HTTPS-only links | source anatomy implemented; app-owned chrome 시각 미검증 |
| business information alert | SwiftUI alert | `BusinessInfoAlertDialog` | app-owned chrome/action 미검증 |
| missing map alert | confirmation dialog | `MissingMapAlertDialog` | app-owned chrome/branch 미검증 |
| other map picker | confirmation dialog | `OtherMapPickerDialog` | app-owned chrome/selection 미검증 |
| Apple map failure | explicit alert | `AppleMapFailureAlertDialog` | app-owned chrome/action 미검증 |
| notification denied | explicit alert | `NotificationDeniedAlertDialog` | app-owned chrome/action 미검증 |
| reminder time picker | system time picker | Android system `TimePickerDialog` | Forced OS exception; resulting value/persistence 미검증 |
| recent/favorites swipe | -76pt reveal and delete/unfavorite | -76dp reveal and custom TalkBack action | gesture threshold/animation/persistence 미검증 |
| keyboard Search/Back | focused region search | Compose field + IME actions | runtime/focus/nav 미검증 |
| app relaunch | UserDefaults persistence | SharedPreferences JSON persistence | unit coverage only; relaunch UI trace 미검증 |
| accessibility | VoiceOver labels/actions/state | TalkBack roles/actions/state | focus order, large text, touch target, announcements 미검증 |
| tablet 600dp+ | centered 600pt rail, portrait/landscape | centered 600dp rail, orientation unlocked | actual tablet resize/capture 미검증 |
| Google TV / D-pad | Android-only requirement | TV launcher declared | focus graph, visible focus, Back, 10-foot layout 미검증 |

## 이번 current-source 반영

- 과거의 근거 없는 Android 확대 예외는 폐기했다. 이후 최신 직접 지시에 따라 current iOS-derived 하단바 anatomy 전체를 Android에서 정확히 1.5배로 적용하고 좌우 28dp, 하단 28dp를 사용했다.
- `지역`과 `최근`은 확대 전 임시 glyph를 재사용하지 않고 current iOS의 `map.fill`, `clock.arrow.circlepath` appearance를 30dp Canvas silhouette로 다시 제작했다.
- 홈을 `wordmark → leather hero → 어디서 드실까요? → location cards` 순서로 바꾸고 위치 카드 폭을 hero stitch rail에 맞췄다.
- wordmark bitmap은 동일 SHA-256 원본을 유지하고 `??`의 size/weight/tracking/overlap/baseline 값을 current iOS source에 맞췄다.
- 공용 content bottom reserve는 확대된 bar 126dp와 하단 28dp의 실제 점유 높이인 154dp로 조정해 모든 route에 적용했다.
- launcher adaptive icon은 opaque Apple 완성 아이콘을 foreground로 채우던 구성을 제거하고, 108dp background와 투명 lunchbox foreground를 분리했다. 13dp inset 후 lunchbox visible width는 약 60.1dp이며 monochrome 핵심 표식은 기존 60dp 안전 영역을 유지한다.
- 전화번호가 있는 결과 main/grid 카드와 결정 화면에 current iOS와 같은 전화 action과 TalkBack label을 추가하고, 표시용 구분 문자를 제거한 `ACTION_DIAL` URI를 단위 테스트했다.
- 식당 API는 iOS와 같은 `https://nasfinder.com/api/restaurants` 응답을 사용하며 앱 안에서 TourAPI/Openverse provider 알고리즘을 복제하지 않는다. Android parser가 버리던 direct `photoURL`과 `distanceMeters`, 사진 메타데이터 9개, `photoMatchEvidence` 5개를 current iOS exact-key 계약으로 교체하고 구형 `photoUrl`/nested `photo`/`distance` alias를 거부하는 fixture를 추가했다.
- 첫 응답에 사진이 없는 항목은 iOS와 같은 0.9초·이후 1.8초 재조회로 보강한다. 현재 표시 순서를 유지하고 같은 id의 최신 응답에 `photoURL`이 생긴 경우만 교체하며, 열린 결정도 같은 id로 갱신한다.
- remote loader는 HTTPS/15초 제한, Foursquare만 cache bypass, 그 외 memory+HTTP cache, provider+URL task identity, fallback-first/Crop/180ms fade를 사용한다. 화면의 구형 상단 `메뉴 예시` badge를 제거하고 remote load와 메타데이터가 모두 있을 때만 iOS와 같은 하단 정보 원을 표시한다.
- 최근/찜에는 image URL과 category 및 사진 메타데이터 9개를 왕복 보존하고, Foursquare URL·메타데이터만 저장하지 않는다. 결과의 작은 카드와 최근/찜 이미지는 자체 corner radius를 제거하고 부모 카드 clip에 맡겼다.
- 최신 iOS `results.png`의 5개 matchup 식당은 모두 remote URL이 없는 fallback 상태다. SHA-256 `13339e9c4e0a99c026e9a66a1a1f31c8445ddc64c61c1c7e3f63dcf7a0730949`는 Jjamppong→Sushi→FoodMain→Bibimbap→Side1 순서의 local fallback 증거일 뿐 remote provider parity 증거가 아니다.
- SM-F968N 실제 결과 화면에서 중앙 추천 원이 stitch를 넘고 선택 label 대비가 낮은 것이 확인되어, 대표님의 Android 전용 직접 지시대로 원 diameter만 68.04dp의 85%인 57.834dp로 줄였다. 36dp 주사위의 크기·shape·dot은 수정하지 않았고, 원+주사위 group을 5.103dp 아래로 이동해 기존 bottom optical edge를 유지했으며 선택된 `추천`은 다른 selected tab과 같은 진한 흰 label+빨간 indicator로 맞췄다.
- loading animation의 정지 frame은 debug `matchup_state=loading`에만 한정했다. 일반 실행과 다른 fixture에서 추천 화면으로 이동한 경우에는 토큰 orbit와 주사위 회전/상하 이동이 계속된다.
- 최신 APK를 SM-F968N에 데이터 유지 설치한 뒤 active physical display `4630946740849765780`에서 12 fixture를 다시 캡처했다. 대표님 후속 지시의 중앙 원/stitch, 흰 selected label/red indicator가 실제 `results.png`에 반영됐고 launcher adaptive mask도 별도 캡처했다.
- 위치 권한을 자동 승인하지 않고 UI에서 `Gangnam`을 수동 검색해 live 응답을 캡처했다. TourAPI/Openverse direct 사진, 각 이미지의 bottom-leading info circle, 전화 action과 Crop이 실제 표시됐다. 사진 정보 sheet 캡처에서 잘못 0px content로 측정되던 header glyph를 발견해 명시적 18dp fork/photo icon으로 수정했다. 이 live 응답은 provider pool의 요청별 shuffle 때문에 iOS와 동일 작품을 보장하는 paired parity 증거는 아니다.

## 남은 runtime blocker

finish-work 승인으로 SM-F968N(Android 16/API 36)에 최신 APK의 데이터 유지 `adb install -r`, cold launch, 12 fixture, launcher, live remote 결과와 photo sheet 캡처까지 수행했다. 다만 current iOS와 Android는 서로 다른 물리 크기/OS chrome이고 remote Openverse pool은 요청별 shuffle이므로 이 증거만으로 broad pixel parity를 선언하지 않는다. 연결된 Android tablet/Google TV 대상이 없어 tablet rail·TV D-pad는 runtime 미검증이며 TalkBack 실제 읽기 순서, swipe 삭제 후 relaunch, 외부 지도 오류 분기도 별도 manual trace가 남았다. 따라서 상태는 **implemented from current iOS source; phone captures available; broad visual parity unverified**다.

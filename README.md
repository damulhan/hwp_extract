# HWP Extractor

Kotlin 기반 HWP/HWPX 파일 텍스트 추출 유틸리티

## 기능

- HWP(한글 2005 이하) 및 HWPX(한글 2007 이상) 파일 지원
- 텍스트 추출
- 메타데이터 추출
- 임베디드 파일 추출
- 암호화된 파일 지원
- 여러 파일 동시 처리

## 요구사항

- Java 17 이상
- Gradle 7.0 이상

## 빌드

```bash
./gradlew build
```

## 사용법

### 기본 사용 (텍스트를 stdout으로 출력)

```bash
./hwp-extract file.hwp
./hwp-extract file.hwpx
```

### 여러 파일 처리

```bash
./hwp-extract file1.hwp file2.hwpx file3.hwp
```

### 옵션

```bash
./hwp-extract [-h] [--debug] [--extract-meta] [--extract-files] \
              [--output-directory OUTPUT_DIRECTORY] [--password PASSWORD] \
              [--version] target_file [target_file ...]
```

#### 옵션 설명

- `-h, --help`: 도움말 표시
- `-d, --debug`: 디버그 모드 활성화
- `-m, --extract-meta`: 메타데이터 추출 (제목, 작성자 등)
- `-f, --extract-files`: 임베디드 파일 추출
- `-o, --output-directory OUTPUT_DIRECTORY`: 출력 디렉토리 지정 (지정하지 않으면 stdout)
- `-p, --password PASSWORD`: 암호화된 파일의 비밀번호
- `-v, --version`: 버전 정보 표시

### 사용 예제

#### 1. 텍스트를 파일로 저장

```bash
./hwp-extract -o output/ document.hwp
```

#### 2. 메타데이터와 함께 추출

```bash
./hwp-extract --extract-meta document.hwp
```

#### 3. 임베디드 파일도 함께 추출

```bash
./hwp-extract --extract-files -o output/ document.hwp
```

#### 4. 암호화된 파일 처리

```bash
./hwp-extract --password mypassword encrypted.hwp
```

#### 5. 디버그 모드로 실행

```bash
./hwp-extract --debug document.hwp
```

#### 6. 모든 옵션 사용

```bash
./hwp-extract --debug --extract-meta --extract-files \
              -o output/ --password mypass document.hwp
```

## Gradle을 통한 직접 실행

```bash
./gradlew run --args="document.hwp"
./gradlew run --args="--extract-meta -o output/ document.hwp"
```

## JAR 파일로 실행

```bash
java -jar build/libs/hwp-extractor-1.0.0.jar document.hwp
```

## 프로젝트 구조

```
hwp_extractor_java/
├── build.gradle.kts          # Gradle 빌드 설정
├── settings.gradle.kts        # Gradle 프로젝트 설정
├── gradle.properties          # Gradle 속성
├── hwp-extract               # Shell 실행 스크립트
├── README.md                 # 이 파일
└── src/
    └── main/
        └── kotlin/
            └── kr/
                └── etna/
                    └── hwpextractor/
                        ├── Main.kt              # CLI 진입점
                        ├── HwpExtractor.kt      # 메인 추출기
                        ├── HwpTextExtractor.kt  # HWP 추출 구현
                        └── HwpxTextExtractor.kt # HWPX 추출 구현
```

## 의존성

- [hwplib](https://github.com/neolord0/hwplib) - HWP 파일 처리
- [hwpxlib](https://github.com/neolord0/hwpxlib) - HWPX 파일 처리
- [Apache Commons CLI](https://commons.apache.org/proper/commons-cli/) - 명령줄 인자 파싱

## 라이선스

이 프로젝트는 사용된 라이브러리들의 라이선스를 따릅니다.

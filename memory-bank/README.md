# Memory Bank (메모리 뱅크)

이 폴더는 **Cursor Memory Bank**용으로, AI가 프로젝트 맥락을 유지하고 일관된 지원을 하도록 돕습니다.

## 파일 설명
| 파일 | 용도 |
|------|------|
| `projectbrief.md` | 프로젝트 목표, 범위, 주요 기능, 제약 |
| `productContext.md` | 제품 목적, 사용자, 핵심 가치 |
| `systemPatterns.md` | 아키텍처, 인증/API 패턴, 기술 결정 |
| `techContext.md` | 기술 스택, 실행 방법, 주요 경로 |
| `activeContext.md` | **현재 작업 초점**, 최근 결정, 다음 할 일 |
| `progress.md` | 완료/진행 중 작업, 이슈, 다음 마일스톤 |

## 사용 방법
1. **일반 개발**: Cursor가 대화 시 이 폴더를 참고해 맥락을 유지합니다.
2. **직접 수정**: 작업이 바뀔 때마다 `activeContext.md`와 `progress.md`를 업데이트하면 도움이 됩니다.
3. **AI에게 요청**: "메모리 뱅크에 반영해줘", "현재 작업 맥락 정리해줘" 등으로 갱신을 요청할 수 있습니다.

## 참고
- Cursor 규칙은 `.cursor/rules/memory-bank.mdc`에 정의되어 있습니다.
- 프로젝트 구조나 스택이 바뀌면 `projectbrief.md`, `systemPatterns.md`, `techContext.md`를 함께 수정하는 것이 좋습니다.

# 당당이 챗봇 전체 기능 테스트 스크립트
# 사용법: 서버(uvicorn main:app --reload)를 먼저 켜둔 상태에서,
#         새 터미널 창에서 .\test_all.ps1 실행

$ErrorActionPreference = "Continue"
$base = "http://localhost:8000"

# curl.exe 등 외부 프로세스 출력을 캡처할 때 한글이 깨지지 않도록
# 콘솔 입출력 인코딩을 스크립트 시작 시점에 UTF-8로 고정
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

function Invoke-Utf8Post($uri, $bodyObj) {
    $json = $bodyObj | ConvertTo-Json -Depth 5 -Compress
    $bytes = [System.Text.Encoding]::UTF8.GetBytes($json)
    return Invoke-RestMethod -Uri $uri -Method Post -ContentType "application/json; charset=utf-8" -Body $bytes
}

Write-Host "`n========== 1. 헬스체크 ==========" -ForegroundColor Cyan
Invoke-RestMethod -Uri "$base/" -Method Get | ConvertTo-Json

Write-Host "`n========== 2. DB에 있는 음식 (김치찌개) ==========" -ForegroundColor Cyan
$r2 = Invoke-Utf8Post "$base/chat" @{
    user_id = "test1"
    message = "나 현재혈당 110이고 김치찌개 먹었어"
    diagnosis_group = "전당뇨"
}
Write-Host $r2.reply
Write-Host "`n[매칭된 음식 및 영양성분]" -ForegroundColor Yellow
$r2.prediction_data | ConvertTo-Json -Depth 5

Write-Host "`n========== 3. DB에 없는 음식 (LLM 추정 폴백) ==========" -ForegroundColor Cyan
$r3 = Invoke-Utf8Post "$base/chat" @{
    user_id = "test1"
    message = "나 혈당 110인데 엄마가 해준 특제 스튜 먹었어"
}
Write-Host $r3.reply
Write-Host "`n[매칭된 음식 및 영양성분 (추정치)]" -ForegroundColor Yellow
$r3.prediction_data | ConvertTo-Json -Depth 5

Write-Host "`n========== 4. 회원가입 -> 진단군 자동 계산 ==========" -ForegroundColor Cyan
$r4 = Invoke-Utf8Post "$base/signup-profile" @{
    user_id = "test2"
    hba1c = 6.0
}
$r4 | ConvertTo-Json

Write-Host "`n========== 5. 회원가입한 유저가 진단군 안 넣고 대화 ==========" -ForegroundColor Cyan
$r5 = Invoke-Utf8Post "$base/chat" @{
    user_id = "test2"
    message = "지금 혈당 130인데 라면 먹었어"
}
Write-Host $r5.reply

Write-Host "`n========== 6. predict-glucose 직접 호출 (경계값 baseline 250) ==========" -ForegroundColor Cyan
$r6 = Invoke-Utf8Post "$base/predict-glucose" @{
    food_name = "갈비탕"
    baseline = 250
    diagnosis_group = "2형당뇨"
}
$r6 | ConvertTo-Json -Depth 5

Write-Host "`n========== 7. 일반 건강 질문 (RAG 연동 시 자동으로 지식 활용) ==========" -ForegroundColor Cyan
$r7 = Invoke-Utf8Post "$base/chat" @{
    user_id = "test1"
    message = "당뇨병 환자는 운동을 얼마나 해야 하나요?"
}
Write-Host $r7.reply

Write-Host "`n========== 8. 음식 사진 인식 ==========" -ForegroundColor Cyan
$imagePath = "C:\Users\smhrd1\Desktop\food.png"
if (Test-Path $imagePath) {
    $r8 = curl.exe -s -X POST "$base/identify-food" -F "image=@$imagePath"
    $r8 | ConvertFrom-Json | ConvertTo-Json -Depth 5
} else {
    Write-Host "이미지 파일을 찾을 수 없음: $imagePath" -ForegroundColor Red
}

Write-Host "`n========== 테스트 끝 ==========" -ForegroundColor Green
Write-Host "결과를 파일로도 저장하려면: .\test_all.ps1 | Out-File -FilePath test_result.txt -Encoding utf8"
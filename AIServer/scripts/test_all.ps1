# 당당이 AI 서버 전체 기능 테스트 스크립트
# 사용법: fastapi_app/ 안에서 서버(uvicorn main:app --reload)를 먼저 켜둔 상태로,
#         새 터미널 창에서 .\test_all.ps1 실행
#
# 2026-08 재구성: 엔드포인트가 /chat, /predict-glucose, /identify-food 등에서
# /rag/chat, /rag/intake-logs/recognize, /rag/intake-logs/reanalyze,
# /rag/intake-logs/predict 로 바뀐 뒤 방치되어 있던 걸 현재 main.py 기준으로 갱신함.
# /signup-profile 처럼 실제로 구현된 적 없는 엔드포인트는 제거함.

$ErrorActionPreference = "Continue"
$base = "http://localhost:8000"

# Spring -> FastAPI 내부 호출 인증 헤더. /rag/* 라우터 전부 이 헤더 없으면 401.
# (2026-08-13 추가된 인증이라, 이 스크립트가 그전에 만들어져서 헤더가 빠져있었음)
# fastapi_app/.env의 INTERNAL_API_KEY와 같은 값이어야 함.
$internalApiKey = "adlQ7E2D3n576Bo9q8oEWeb0D6UO9upZCN4lx56nx"
$authHeaders = @{ "X-Internal-Api-Key" = $internalApiKey }

# curl.exe 등 외부 프로세스 출력을 캡처할 때 한글이 깨지지 않도록
# 콘솔 입출력 인코딩을 스크립트 시작 시점에 UTF-8로 고정
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

function Invoke-Utf8Post($uri, $bodyObj) {
    $json = $bodyObj | ConvertTo-Json -Depth 5 -Compress
    $bytes = [System.Text.Encoding]::UTF8.GetBytes($json)
    return Invoke-RestMethod -Uri $uri -Method Post -Headers $authHeaders -ContentType "application/json; charset=utf-8" -Body $bytes
}

# recognize/reanalyze 엔드포인트는 FastAPI에서 File(...)/Form(...)으로 받기 때문에
# JSON 바디가 아니라 form 형식(application/x-www-form-urlencoded)으로 보내야 한다.
# (JSON으로 보내면 서버가 message/food_name을 못 읽어서 "필수입니다" 에러가 남)
function Invoke-Utf8FormPost($uri, $bodyObj) {
    Add-Type -AssemblyName System.Web
    $pairs = $bodyObj.GetEnumerator() | ForEach-Object {
        "$($_.Key)=$([System.Web.HttpUtility]::UrlEncode([string]$_.Value, [System.Text.Encoding]::UTF8))"
    }
    $form = $pairs -join "&"
    $bytes = [System.Text.Encoding]::UTF8.GetBytes($form)
    return Invoke-RestMethod -Uri $uri -Method Post -Headers $authHeaders -ContentType "application/x-www-form-urlencoded" -Body $bytes
}

Write-Host "`n========== 1. 헬스체크 ==========" -ForegroundColor Cyan
Invoke-RestMethod -Uri "$base/" -Method Get | ConvertTo-Json

Write-Host "`n========== 3. 음식 인식 - 텍스트, DB에 없는 음식 (matched=false 확인) ==========" -ForegroundColor Cyan
$r3 = Invoke-Utf8FormPost "$base/rag/intake-logs/recognize" @{
    message = "엄마가 해준 특제 스튜"
    diagnosis_group = "건강군"
}
$r3 | ConvertTo-Json -Depth 5

Write-Host "`n========== 6. 일반 건강 질문 (RAG 논문 기반 답변) ==========" -ForegroundColor Cyan
$r6 = Invoke-Utf8Post "$base/rag/chat" @{
    user_no = 1
    message = "당뇨병 환자는 운동을 얼마나 해야 하나요?"
}
Write-Host $r6.reply


<#
Write-Host "`n========== 2. 음식 인식 - 텍스트, DB에 있는 음식 (김치찌개) ==========" -ForegroundColor Cyan
$r2 = Invoke-Utf8FormPost "$base/rag/intake-logs/recognize" @{
    message = "구구콘"
    baseline = 110
    diagnosis_group = "전당뇨"
}
$r2 | ConvertTo-Json -Depth 5





Write-Host "`n========== 4. AI 재분석 - '틀려요, AI로 분석하기' (텍스트) ==========" -ForegroundColor Cyan
$r4 = Invoke-Utf8FormPost "$base/rag/intake-logs/reanalyze" @{
    food_name = "구구콘"
    baseline = 110
    diagnosis_group = "전당뇨"
}

$r4 | ConvertTo-Json -Depth 5

Write-Host "`n========== 5. portion 반영 재예측 (최종 확정 직전) ==========" -ForegroundColor Cyan
$r5 = Invoke-Utf8Post "$base/rag/intake-logs/predict" @{
    carb = 40; sugar = 3; protein = 15; fat = 10; calorie = 350; fiber = 2
    portion = 1.5
    baseline = 250
    diagnosis_group = "2형당뇨"
}
$r5 | ConvertTo-Json -Depth 5



Write-Host "`n========== 7. 약물 용량 질문 차단 확인 ==========" -ForegroundColor Cyan
$r7 = Invoke-Utf8Post "$base/rag/chat" @{
    user_no = 1
    message = "인슐린 용량을 늘려도 될까요?"
}
Write-Host $r7.reply

Write-Host "`n========== 8. 음식 사진 인식 (image가 있을 때만 실행) ==========" -ForegroundColor Cyan
$imagePath = "C:\Users\smhrd1\Desktop\food.png"
if (Test-Path $imagePath) {
    $r8 = curl.exe -s -X POST "$base/rag/intake-logs/recognize" -H "X-Internal-Api-Key: $internalApiKey" -F "image=@$imagePath" -F "diagnosis_group=전당뇨"
    $r8 | ConvertFrom-Json | ConvertTo-Json -Depth 5
} else {
    Write-Host "이미지 파일을 찾을 수 없음: $imagePath (건너뜀)" -ForegroundColor Yellow
}

Write-Host "`n========== 테스트 끝 ==========" -ForegroundColor Green
Write-Host "결과를 파일로도 저장하려면: .\test_all.ps1 | Out-File -FilePath test_result.txt -Encoding utf8"

#>

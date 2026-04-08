param(
    [string]$Source = "Azkar - Copy - Copy\data.js",
    [string]$OutputDir = "app\src\main\assets\native"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function Decode-JsString([string]$value) {
    $value = $value -replace '\\n', "`n"
    $value = $value -replace '\\r', ""
    $value = $value -replace '\\"', '"'
    $value = $value -replace "\\\\", "\"
    return $value
}

function Get-JsonLiteral([string]$text, [string]$name) {
    $startMatch = [regex]::Match($text, "const\s+$name\s*=\s*([\{\[])", [System.Text.RegularExpressions.RegexOptions]::Singleline)
    if (-not $startMatch.Success) {
        throw "Could not find literal for $name"
    }

    $open = $startMatch.Groups[1].Value
    $close = if ($open -eq "{") { "}" } else { "]" }
    $index = $startMatch.Groups[1].Index
    $depth = 0
    $inString = $false
    $escaped = $false

    for ($i = $index; $i -lt $text.Length; $i++) {
        $char = $text[$i]

        if ($inString) {
            if ($escaped) {
                $escaped = $false
                continue
            }
            if ($char -eq '\') {
                $escaped = $true
                continue
            }
            if ($char -eq '"') {
                $inString = $false
            }
            continue
        }

        if ($char -eq '"') {
            $inString = $true
            continue
        }

        if ($char -eq $open) {
            $depth++
        } elseif ($char -eq $close) {
            $depth--
            if ($depth -eq 0) {
                return $text.Substring($index, ($i - $index + 1))
            }
        }
    }

    throw "Could not parse literal for $name"
}

function Convert-JsLiteralToJson([string]$literal, [hashtable]$constants) {
    $json = $literal
    $json = [regex]::Replace($json, '(?m)^\s*//.*$', '')

    foreach ($entry in $constants.GetEnumerator() | Sort-Object { $_.Key.Length } -Descending) {
        $replacement = ($entry.Value | ConvertTo-Json -Compress)
        $pattern = "(?<![A-Za-z0-9_])$([regex]::Escape($entry.Key))(?![A-Za-z0-9_])"
        $json = [regex]::Replace($json, $pattern, [System.Text.RegularExpressions.MatchEvaluator]{ param($m) $replacement })
    }

    $json = [regex]::Replace(
        $json,
        '([{\[,]\s*)([A-Za-z_][A-Za-z0-9_]*)(\s*:)',
        '$1"$2"$3'
    )

    $json = [regex]::Replace($json, ',(\s*[}\]])', '$1')
    return $json
}

$sourcePath = Join-Path (Get-Location) $Source
$outputPath = Join-Path (Get-Location) $OutputDir
New-Item -ItemType Directory -Force -Path $outputPath | Out-Null

$content = Get-Content -LiteralPath $sourcePath -Raw -Encoding UTF8

$constants = @{}
$constantMatches = [regex]::Matches(
    $content,
    'const\s+([A-Z0-9_]+)\s*=\s*"((?:\\.|[^"\\])*)";',
    [System.Text.RegularExpressions.RegexOptions]::Singleline
)

foreach ($match in $constantMatches) {
    $constants[$match.Groups[1].Value] = Decode-JsString $match.Groups[2].Value
}

$targets = @("sunanData", "popularCities", "allCities", "azkarData")
foreach ($target in $targets) {
    $literal = Get-JsonLiteral -text $content -name $target
    $json = Convert-JsLiteralToJson -literal $literal -constants $constants
    $parsed = $json | ConvertFrom-Json
    $serialized = $parsed | ConvertTo-Json -Depth 100
    Set-Content -LiteralPath (Join-Path $outputPath "$target.json") -Value $serialized -Encoding UTF8
}

Write-Output "Exported native assets to $outputPath"

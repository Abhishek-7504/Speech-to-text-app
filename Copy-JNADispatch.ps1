# Set paths
$projectRoot = "C:\Users\Abhishek\AndroidStudioProjects\VoskApplication"
$jnaExtractPath = "$projectRoot\libs\jna-temp\jni"
$jniLibsPath = "$projectRoot\app\src\main\jniLibs"

# List of architectures to process
$architectures = @("arm64-v8a", "armeabi-v7a", "x86", "x86_64")

foreach ($arch in $architectures) {
    $sourceFile = Join-Path -Path $jnaExtractPath -ChildPath "$arch\libjnidispatch.so"
    $destinationDir = Join-Path -Path $jniLibsPath -ChildPath $arch
    $destinationFile = Join-Path -Path $destinationDir -ChildPath "libjnidispatch.so"

    if (Test-Path $sourceFile) {
        # Create destination directory if it doesn't exist
        if (-not (Test-Path $destinationDir)) {
            New-Item -ItemType Directory -Path $destinationDir -Force | Out-Null
        }

        # Copy the file
        Copy-Item -Path $sourceFile -Destination $destinationFile -Force
        Write-Host "✅ Copied for architecture: $arch"
    } else {
        Write-Warning "⚠️ Source file not found for $arch: $sourceFile"
    }
}

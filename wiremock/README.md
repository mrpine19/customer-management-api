# WireMock local setup

This folder contains local stubs to simulate the external score service.

## Stub files

- `mappings/score-success.json`
- `mappings/score-not-found.json`
- `mappings/score-server-error.json`
- `mappings/score-timeout.json`
- `mappings/score-invalid-body.json`

## Run with Docker

```powershell
docker run --rm -it -p 8081:8080 -v "${PWD}\wiremock:/home/wiremock" wiremock/wiremock:3.9.1
```

## Test stubs

```powershell
Invoke-RestMethod "http://localhost:8081/scores/12345678901"
Invoke-WebRequest "http://localhost:8081/scores/98765432100"
Invoke-WebRequest "http://localhost:8081/scores/11122233344"
Invoke-RestMethod "http://localhost:8081/scores/55566677788"
Invoke-WebRequest "http://localhost:8081/scores/99988877766"
```


@echo off
echo Starting simple HTTP server on port 3000...
echo Open http://localhost:3000 in your browser
echo Press Ctrl+C to stop the server
cd /d "%~dp0"
python -m http.server 3000


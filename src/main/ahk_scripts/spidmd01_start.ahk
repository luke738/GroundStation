SetTitleMatchMode, 2
Run, "C:\Users\Luke\Downloads\spidMD01dde.exe", , , PID
WinWait, ahk_pid %PID%
WinMove, ahk_pid %PID%, , (A_ScreenWidth/2), 0
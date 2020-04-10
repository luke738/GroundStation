SetTitleMatchMode, 2
Run, "C:\Program Files (x86)\Orbitron\Orbitron.exe", , , PID
WinWait, ahk_pid %PID%
WinMove, ahk_pid %PID%, , 0, 0, (A_ScreenWidth/2), (A_ScreenHeight/2)
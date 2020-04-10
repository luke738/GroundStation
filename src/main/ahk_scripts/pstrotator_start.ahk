SetTitleMatchMode, RegEx
Run, "C:\Program Files (x86)\PstRotator\PstRotator.exe", , , PID
MsgBox, "Please wait for PstRotator to fully open, then click ok."
WindowTitle := "PstRotator"
WinWait, % WindowTitle
WinMove, % WindowTitle, , (A_ScreenWidth/2), 200
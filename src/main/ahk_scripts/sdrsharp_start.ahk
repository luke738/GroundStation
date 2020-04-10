SetTitleMatchMode, RegEx
Run, "C:\Users\Luke\Desktop\SDRSharp\SDRSharp.exe", , , PID
WindowTitle := "SDR#"
WinWait, % WindowTitle
WinMove, % WindowTitle, , 0, (A_ScreenHeight/2), (A_ScreenWidth/2), (A_ScreenHeight/2)-40
set PATH_TO_FX="D:\Program Files\Java\javafx-sdk-15.0.1\lib"
set PATH_TO_JDK="C:\Users\vikto\.jdks\openjdk-15.0.2\bin\javaw.exe"
set JAR_TO_LAUNCH="Scanner3D.jar"
%PATH_TO_JDK% --module-path %PATH_TO_FX% --add-modules javafx.controls,javafx.swing,javafx.base,javafx.graphics -jar %JAR_TO_LAUNCH%
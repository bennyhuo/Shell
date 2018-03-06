# Shell
Android Shell run without root. Just like adb shell does.

A server runs in the background with shell permissions while client app should connect to it and send shell cmd to it though a socket connection.

The shell session closes until the connect disconnect.

You can achieve varieties of goals with this, e.g. take a bugreport with pc.

# Thanks

This project is mainly inspired by [Fairy](https://github.com/Zane96/Fairy), most of the server code and scripts are reuse in this project. You should install a server started by adb shell from pc so that it has shell permissions.
![ServerCommander](https://user-images.githubusercontent.com/33269270/207451491-55d576e8-ad3e-485e-8fb9-508d8dbb8dbb.png)
# ServerCommander
[![CodeFactor](https://www.codefactor.io/repository/github/morganmlgman/servercommander/badge)](https://www.codefactor.io/repository/github/morganmlgman/servercommander)

- [ServerCommander compatibility](https://github.com/MorganMLGman/ServerCommander#servercommander-compatibility)
- [Why YunoHost?](https://github.com/MorganMLGman/ServerCommander#why-yunohost)
- [Why Docker?](https://github.com/MorganMLGman/ServerCommander#why-docker)
- [Known bugs](https://github.com/MorganMLGman/ServerCommander#known-bugs-)
- [Changelog](https://github.com/MorganMLGman/ServerCommander#changelog)
- [Privacy Policy](https://github.com/MorganMLGman/ServerCommander#privacy-policy)
- [Thanks to](https://github.com/MorganMLGman/ServerCommander#thanks-to)

---
Mobile application that helps manage self-hosted homelab. The mobile client application is a great tool that allows to manage your server quickly, conveniently and almost anytime. 
---

Within **ServerCommander** you can check such things as:
- CPU temperature
- CPU usage
- RAM usage
- Linux kernel version
- Your local and public IP
- System uptime
- Disk usage
- Most demanding app at the moment
- Number of installed packages
- And much much more (new features will be released in app updates)

If your server is running Docker in addition you can _**start/stop/restart**_ containers, briefly view containers summary and check statistics about single container.

If you are running YunoHost you can see ***how many users, backups and domains*** you have. Also there is a possibility to see ***how many applications are waiting for an update*** and ***you can list them***. You can ***push new ssh keys*** for a chosen user.

However no matter what type of server you have, you can always perform actions like:
- Server shutdown
- Server reboot
- Check system updates
- Perform system update
- (New features will be released in upcoming app updates)

## ServerCommander compatibility

App is compatible with servers running:
- [YunoHost](https://github.com/YunoHost)
- [Docker](https://github.com/docker)

under **Debian** and **Debian-based** operating system. 

Our app has been tested under these systems:
- Debian 11
- Ubuntu 22.04, Ubuntu 22.10
- Raspberry Pi OS

## Why YunoHost?

YunoHost is a great system that helps run own server without professional IT knowledge. That's why it is a great option for a lot of users that want to have own, private server with services, tools and applications. WebAdmin is a great option to manage server, but it is not that comfortable on mobile devices like it is on PCs. 

## Why Docker?

Docker is very powerful tool to host many applications on one machine. One of **ServerCommander** developers is using such machine :) Self-hosting with Docker is very flexible, you can choose one of thousands containers with the application you like and deploy it in a breeze.

## Known bugs :)
- SwipeRefreshLayout on _SYSTEM_ and _DOCKER_ tabs in not returning to default state when _PasswordAlertDialog_ is dismissed by clicking outside the dialog or pressing back button.
- App sometimes crashes when _TestConnection_ is ongoing and connection settings in _SETTINGS_ tab are changed.
- App can crash when YunoHost server URL is not valid.

## Changelog

### [v20221213.202445](https://github.com/MorganMLGman/ServerCommander/releases/tag/v20221213.202445)

This is out first _**"production"**_ ready release. Feel free to check it out and submit issues. 

## Privacy Policy

**Our app is not collecting any user data.**

We have never collected, do not collect, nor will we collect any user data, usage statistics or any other data about app users, their system or other installed applications.

If you're still not convinced that our app is in a **completely anonymous** state, you're in the perfect place, the app code is **fully open source** and is available at https://github.com/MorganMLGman/ServerCommander

## Thanks to

- [Docker authors](https://github.com/docker)
- [YunoHost authors](https://github.com/YunoHost)
- [Cedrica from DCU](https://dribbble.com/shots/3896634-Profile-Screens) for dashboard idea and [Chirag Kachhadiya](https://www.youtube.com/watch?v=ZjAxAw0kmrY) for showing how to make it real
- Intuit developers for providing [sdp](https://github.com/intuit/sdp) library

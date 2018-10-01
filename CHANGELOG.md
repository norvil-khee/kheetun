# Change Log
All notable changes to this project will be documented in this file.

## 0.13.3 - 2018-10-01

### Added

- Support for Java 10+ (including jaxb dependencies)


## 0.13.2 - 2017-03-13

### Fixed

- Pings should not appear in shell history on tunneling server

### Added

- Dialog asking if all running tunnels should be stopped before exit
- Passphrase dialog for encrypted SSH keys

## 0.13.1 - 2017-03-01

### Fixes

- Fixed postinstall log4j2.xml file creation

## 0.13.0 - 2017-02-05

### Added

- The configuration GUI is back

### Changed

- Using Jsch version 0.1.54
- improved load a little bit by removing unnecessary threads
- LOG4J configuration now in /opt/kheetun/etc/log4j2.xml for server
- LOG4J configuration now in $HOME/.kheetun/log4j2.xml for client

### Fixes

- Ping daemon changed to not open new SSH channels each time ping is measured.
- Rampant autostart daemons removed

## 0.12.3 - 2016-11-18

### Added

- Sorting of profiles (currently alphabetical and by config file modification time).
- Tunnel configuration: port can now be specified.
- Profiles may be deactivated in config files by setting the ``active`` property of the ``<profile>`` tag (``true`` or ``false``).
- Additional information for autostarted tunnels is added (why the "waiting" status is displayed).
- Displaying a loading icon when configuration is (re)loaded.

### Removed

- Pre 0.9.0 configuration files will no longer be transformed into new ones. The plan is to handling this in post-install scripts in the future - if still necessary.

### Changed

- Tunnel handling is now done entirely on server side. The client only sends configurations and triggers manual starts and stops of tunnels.
- A few style changes in tray menu.

### Fixes

- Fixed a few concurrency issues.
- Fixed client side reconnection issues.

## 0.10.1 - 2016-11-03

### Added

- tray icon turns red if errors are displayed (no matter if read or unread)
- middle-click copies error message (if present)

### Changed

- internal code refactoring
- invalid configurations will now be displayed as invalid separately
- connection to daemon (and therefore handling of tunnels) will be done even if some configurations are invalid

### Fixes

- read only XML files below ``kheetun.d``

## 0.10.0 - 2016-10-27

### Added
- stale tunnels (tunnels which are running, but not defined in config anymore) are now stopped before new configuration will be applied
- config file conversion from 0.9.0 to new layout
- once started, also non-autostart tunnels will be restarted if they failed (e.g. due to a ping timeout)
- added an "autostart all" button to reenable autostarting of previously failed or manually stopped tunnels
- added maximum attempts of retries after connection failures of autostarting tunnels - this is configureable in XML files as "maxFailures" property of a ``<tunnel>`` - the default being 3.
- trigger tray menu on right-click and left-click

### Changed
- switched from "single configuration" file layout to "multiple configuration" file layout
- XML configuration files are now to be stored in $HOME/.kheetun/kheetun.d, where each file contains one ``<profile>`` block
- global configuration is now done in $HOME/.kheetun/kheetun.conf
- deprecated configuration files will be converted to new layout (XML files named after profile name, lower case)
- deprecated configuration will be renamed to ``kheetun.xml.deprecated``
- yet another tray menu redesign
- much internal code refactoring

### Fixes
- ``/etc/hosts`` entry manager occasionally swallowed the last line

## 0.9.0 - 2016-10-18

### Added
- configuration changes handled while running
- reconnect on config changes
- autoconnect when daemon is started

### Changed
- config now done in XML only

### Removed
- removed configuration dialog because it was ugly

### Fixes
- stop all disables autostart temporary
- ping daemon on server does not send ping updates if client is disconnected
- stop displaying autostart symbol for tunnels that were stopped manually


## 0.8.0 - 2016-10-10 

### Added
- failure and error messages now in tray

### Removed
- error popups removed



## 0.7.0 - 2016-10-03

### Added
- implemented autostarting

### Changed
- redesigned tray, now GTK native (but working)

### Fixes
- display fixes for tray menu



## 0.6.0 - 2016-10-01

### Added
- added ping output



## 0.5.0 - 2015-06-01

### Added 
- added (java) tray



## 0.4.0 - 2015-05-01

### Added
- added SSH agent authentication

### Fixes
- improved GUI



## 0.1.0

### Added
- initial version
- define tunnels and start them via gui


# Change Log
All notable changes to this project will be documented in this file.

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


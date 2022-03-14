# BlockScreen 🖥️

## Usage

Note: This can only be used locally, servers generally don't have capturable screens

First, [download the latest jar](https://github.com/emortaldev/BlockScreen/releases/latest/download/BlockScreen-1.0.0-all.jar), then to start:

```java
java -Xmx5G -Xms5G -jar BlockScreen-1.0.0-all.jar
```
or, if you'd like to set a custom width and height in-game:
```java
java -Xmx5G -Xms5G -DcustomBlockWidth=20 -DcustomBlockHeight=10 -jar BlockScreen-1.0.0-all.jar
```
making this smaller sacrifices quality, however can make the performance much more managable (This defaults to screen size ÷10 so `192` and `108` on a 1080p monitor)

On first startup, BlockScreen will automatically download and unzip the Minecraft textures and average their colours for use later. This can take a while as the download is around ~400mb.

Once in-game, take in the amazing graphics! 🤯 BlockScreen will copy your screen into Minecraft! (if everything is black, there's likely an [issue](https://github.com/emortaldev/BlockScreen/issues/new))

You can also use some commands:

| Command Usage | Description |
-|:-
| /dither | Toggles [dithering](https://en.wikipedia.org/wiki/Floyd%E2%80%93Steinberg_dithering) |
| /scaletype <scaletype> | Sets the scale type, there are only really 2 distinct types which are smooth and default

## Compile
First, clone this repository
```
git clone https://github.com/emortaldev/BlockScreen/
```
Then compile with gradle
```yaml
# Windows
gradlew.bat build
  
# Linux & MacOS
./gradlew build
```

# <img src="src/main/resources/stickerify.svg" align="right" width="100">[Stickerify](https://t.me/StickerifyImageBot)

A Telegram bot to convert images in the format required to be used as Telegram stickers (512x512 PNGs)

## Table of contents

1. [How to use the bot](#How-to-use-the-bot)
2. [How to create a new sticker](#How-to-create-a-new-sticker)
3. [How to set up the project](#How-to-set-up-the-project)
4. [How to run the bot locally](#How-to-run-the-bot-locally)
5. [How to contribute to the project](#How-to-contribute-to-the-project)
6. [Useful resources](#Useful-resources)
7. [License](#License)

## How to use the bot

The bot can be found [here](https://t.me/StickerifyImageBot): start it, and you can now send it the images you need to convert.

Based on what you send, [Stickerify](https://t.me/StickerifyImageBot) will answer the following:

* the converted image, if you sent a supported file (currently `.gif`s, `.gifv`s and `.webp`s are not supported)
* an error message, if you sent an unsupported file
* an informative message for any message without a file

## How to create a new sticker

1. Chat with [Stickers](https://t.me/Stickers), and use it to create a sticker pack
2. Ask [Stickers](https://t.me/Stickers) to add a new sticker
3. Use [Stickerify](https://t.me/StickerifyImageBot) to create the proper file for your sticker
4. Forward [Stickerify](https://t.me/StickerifyImageBot)'s message to [Stickers](https://t.me/Stickers)
5. Choose an emoji representing your sticker

And it's done!

## How to set up the project

1. Install Maven and JDK 18 (or higher)
2. Clone the project and move into its folder
3. Run the command `mvn install -DskipTests`
4. Import the project inside your IDE as a Maven project
5. Ensure your IDE is correctly configured to use a Java 18 (or higher) JDK

## How to run the bot locally

After you successfully set up the project, you will have to go through the following steps:

1. Chat with [BotFather](https://t.me/BotFather) and ask it to create a new bot
2. Copy the token it provided you and either:
   * set it as the value of a new environment variable named `STICKERIFY_TOKEN` 
   * use it as the return value of the method `getBotToken()` inside `StickerifyBot`
3. Run the method `Main.main()` to start the bot, it will be now able to answer messages in Telegram

## How to contribute to the project

Take a look at this project's contributing guidelines [here](CONTRIBUTING.md).

Do you have any question? Feel free to [open a new discussion](https://github.com/rob93c/Stickerify/discussions/new).

## Useful resources

* [Telegram Bot API](https://core.telegram.org/bots)
* [TelegramBots Java library](https://github.com/rubenlagus/TelegramBots)
* [Railway](https://railway.app?referralCode=rob)
* [Apache Tika](https://tika.apache.org/)

## License

See the [**LICENSE**](LICENSE) file for license rights and limitations (MIT).

<div align="right">
<sup>made with ❤️ by <a href="https://github.com/rob93c">@rob93c</a></sup>
</div>
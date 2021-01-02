# OPoster
OPoster is a planning, scheduling and posting to social media networks.
Publishing with OPoster is easy:

1. Create content
2. Attach one or more photos
3. Select one or more channels to publish (for example: some telegram channel, facebook page, etc.)
4. Specify **when** to post
5. Wait and relax:)

Inspired by feedbacks after the following articles about Orienteer:

* ENG: [How to Build Posts Scheduler with Almost No-Coding](https://medium.com/orienteer/how-to-build-posts-scheduler-with-almost-no-coding-b52068f8c23b)
* RUS: [Свой сервис отложенного постинга и почти без кода](https://habr.com/ru/company/orienteer/blog/530388/)

### OPoster supports:

- [X] Telegram
- [X] Facebook
- [X] Vkontakte

### Planned to support

- [ ] Instagram
- [ ] Odnoklassniki
- [ ] Twitter
- [ ] Pikabu
- [ ] Yandex Dzen
- [ ] TikTok
- [ ] Tumblr
- [ ] Pinterest

## Social Media Setup Guide

Content is universal is can be sent to any supported platforms. But setup of platforms and channels requires some social-media-network related knowledge.

|Nature |OClass    |Description|
|----------|--------|------------------------------------------------------------------------|
|Content |OPContent | Universal content definition. You can specify `title`, `content`, `when`, `images`|
|Channel |OPChannel | Abstract root class for all classes which social-media-network specific. For example for Facebook you should use `OPFacebookPage` and etc. Channels definition should contain everything required to allow OPoster to send to the channel. Please ready below specifics|
| Platform App |OPPlatformApp | Abstract root class for an application on some social-media-network which will reflect your OPoster in that network and allows to post content. Platform App definition should contains everthing required to connect to corresponding network. |

### Telegram

| Nature | OClass | Property | Description |
|--------|--------|----------|-------------|
| Platform App | OPTelegramBot |   | Class to specify Telegram Bot details which will be used to distribute your content |
| Platform App | OPTelegramBot | token | Token which you should obtain from [@BotFather](https://t.me/botfather) after Bot creation |
| Platform App | OPTelegramChannel |   | Class to describe channel or group in telegram to distribute your content to |
| Platform App | OPTelegramChannel | telegramChatId  | `ChatId` of a channel or group where to send content to. You can use [GetIDs Bot](https://t.me/getidsbot) to obtain that `chatId`: either forward message from your channel to this bot or add it to your group - it will display required `chatId`. Do not forget to add your bot into this channel/group! |

### Facebook

### VKontakte

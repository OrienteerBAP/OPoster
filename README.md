[![Build Status](https://travis-ci.org/OrienteerBAP/OPoster.svg?branch=master)](https://travis-ci.org/OrienteerBAP/OPoster) [![Docker Pulls](https://img.shields.io/docker/pulls/orienteer/oposter.svg)](https://hub.docker.com/r/orienteer/oposter/)

# OPoster
OPoster is a planning, scheduling and posting to social media networks.
Publishing with OPoster is easy:

1. Create **what** to post (aka content: text with some attached photos)
2. Select **where** to publish the post: one or more channels (for example: some telegram channel, facebook page, etc.)
3. Specify **when** to post
4. Relax and wait:)

Inspired by feedbacks after the following articles about Orienteer:

* ENG: [How to Build Posts Scheduler with Almost No-Coding](https://medium.com/orienteer/how-to-build-posts-scheduler-with-almost-no-coding-b52068f8c23b)
* RUS: [Свой сервис отложенного постинга и почти без кода](https://habr.com/ru/company/orienteer/blog/530388/)

### OPoster supports:

- [X] [Telegram](#telegram)
- [X] [Facebook](#facebook)
- [X] [Vkontakte](#vkontakte)
- [X] [Instagram](#instagram)
- [X] [Twitter](#twitter)
- [X] [Odnoklassniki](#odnoklassniki)

### Planned to support

- [ ] Pikabu
- [ ] Yandex Dzen
- [ ] TikTok
- [ ] Tumblr
- [ ] Pinterest

If you need some network which is currently not in the list: please [create an issue](https://github.com/OrienteerBAP/OPoster/issues).

## Installation Guide

There are 2 ways how you can install OPoster and start using it:

1. Standalone installation via [docker image](https://hub.docker.com/repository/docker/orienteer/oposter)
2. As add-on module on pre-installed [Orienteer](https://github.com/OrienteerBAP/Orienteer) instance

### OPoster as standalone docker application

You can start OPoster by the following command:

```
docker run -it -p8080:8080 orienteer/oposter
```

For more advanced usage, it's recommended to used `docker-compose`. Here is template of `docker-compose.yml` file:

```yml
version: '2.1'
services:
   orienteer:
      image: orienteer/oposter:latest
      container_name: <Container name>
      network_mode: 'bridge'
      healthcheck:
          test: ["CMD-SHELL", "curl --fail -s -I http://localhost:8080 | grep 'HTTP/1.1' || exit 1"]
          interval: 5m
          timeout: 5s
      ports:
          - "8080:8080"
      volumes:
          - ./runtime:/app/runtime
          - ./maven-repository:/app/repository
      environment:
          - ORIENTDB_ADMIN_PASSWORD=<Admin Password>
          - ORIENTDB_GUEST_PASSWORD=<Guess Password?
          - JAVA_TOOL_OPTIONS=-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/runtime/heapdump.bin
```

### OPoster as add-on module on pre-installed Orienteer

Please follow the following instruction:

1. Login into your Orienteer instance under user with admin rights
2. Navigate to page **Schema** and open tab **Artifacts**
3. Click **Add**
4. In modal window click **Available Orienteer Modules**
5. Wait while list of modules will be loaded
6. Select `org.orienteer.oposter`/`oposter`
7. Click **Install as Trusted** (or **Install as Untrusted** if you are not sure in stability of OPoster)
8. Click **Reload Orienteer**
9. After awaiting ~30 seconds (depends on how heavy is your Orienteer instance) OPoster is installed as add-on and ready to be used

## Formats Coverage

| Network | Only Text | Text with one image | Text with multiple images |
|---------|-----------|---------------------|---------------------------|
| Telegram | :white_check_mark: | :white_check_mark: | :white_check_mark: |
| Facebook | :white_check_mark: | :white_check_mark: | :x: |
| Vkontakte | :white_check_mark: | :white_check_mark: | :white_check_mark: |
| Instagram | :white_check_mark: | :white_check_mark: | :white_check_mark: |
| Twitter | :white_check_mark: | :white_check_mark: | :white_check_mark: |
| Odnoklassniki | :white_check_mark: | :white_check_mark: | :white_check_mark: |

## Social Media Setup Guide

Content is universal is can be sent to any supported platforms. But setup of platforms and channels requires some social-media-network related knowledge.

|Nature |OClass    |Description|
|----------|--------|------------------------------------------------------------------------|
|Content |OPContent | OClass for universal content definition. You can specify `title`, `content`, `when`, `images`|
|Channel |OPChannel | Abstract root OClass for all classes which social-media-network specific. For example for Facebook you should use `OPFacebookPage` and etc. Channels definition should contain everything required to allow OPoster to send to the channel. Please ready below specifics|
| Platform App |OPPlatformApp | Abstract root OClass for an application on some social-media-network which will reflect your OPoster in that network and allows to post content. Platform App definition should contains everthing required to connect to corresponding network. |

### Telegram

| Nature | OClass | Property | Description |
|--------|--------|----------|-------------|
| Platform App | OPTelegramBot |   | OClass to specify Telegram Bot details which will be used to distribute your content |
| Platform App | OPTelegramBot | token | Token which you should obtain from [@BotFather](https://t.me/botfather) after Bot creation |
| Channel | OPTelegramChannel |   | OClass to describe channel or group in telegram to distribute your content to |
| Channel | OPTelegramChannel | telegramChatId  | `ChatId` of a channel or group where to send content to. You can use [GetIDs Bot](https://t.me/getidsbot) to obtain that `chatId`: either forward message from your channel to this bot or add it to your group - it will display required `chatId`. Do not forget to add your bot into this channel/group! |

#### Read More

* [How to create Telegram Bot and obtain Token](https://core.telegram.org/bots#3-how-do-i-create-a-bot)
* [Home page for GetIDsBot telegram bot](https://github.com/wjclub/telegram-bot-getids)

### Facebook

| Nature | OClass | Property | Description |
|--------|--------|----------|-------------|
| Platform App | OPFacebookApp |       | OClass for specifying details about Facebook Application which needs to be created to post with OPoster |
| Platform App | OPFacebookApp | appId | Application Identification which corresponds to application created in Facebook. Please check settings page of your app |
| Platform App | OPFacebookApp | appSecret | Application Secret string which was defined/generated by Vkontakte for your application |
| Platform App | OPFacebookApp | appAccessToken | Access Token for your application. Can be obtained automatically  |
| Platform App | OPFacebookApp | appAccessTokenExpires | Expiration date-time for access token of your application |
| Channel | OPFacebookConnection |         | OClass for defining details about destination to post in Facebook |
| Channel | OPFacebookConnection | facebookId | Id of user or group or page to post to. Should be positive or null. |
| Channel | OPFacebookConnection | page | Boolean flag to show that `facebookId` is actually id of a page. |
| Channel | OPFacebookConnection | acessToken | Access Token for user or page to post. Can be obtained by clicking to button **Connect OAuth** |

#### Read More

* [How to create Facebook App](https://developers.facebook.com/docs/development/create-an-app)
* [How to get profile or page id](https://hellboundbloggers.com/find-facebook-profile-and-page-id/8516/)

### VKontakte

| Nature | OClass | Property | Description |
|--------|--------|----------|-------------|
| Platform App | OPVkApp |       | OClass for specifying details about Vkontakte Application which needs to be created to post with OPoster |
| Platform App | OPVkApp | appId | Application Identification which corresponds to application created in VKontakte. Please check settings page of your app |
| Platform App | OPVkApp | appSecret | Application Secret string which was defined/generated by Vkontakte for your application |
| Platform App | OPVkApp | serviceToken | Service Token which was defined/generated by Vkontakte for your application |
| Platform App | OPVkApp | defaultUserId | Identification (integer) of a user to be used by default for posting on vkontakte. You can take it on your vk user's wall by copying and extracting id from link to photos. Commonly looks like `https://vk.com/albums1234567?profile=1`: here is `1234567` is userId |
| Platform App | OPVkApp | defaultUserAcessToken | Access Token for user specified by id previously. Navigate to `https://oauth.vk.com/authorize?client_id=<You App Id>&display=page&redirect_uri=https://oauth.vk.com/blank.html&scope=offline,wall,groups,video,photos&response_type=token&v=5.52`, click accept and extract token from final page URL |
| Channel | OPVkWall |         | OClass for defining details about wall in vkontakte you are going to post on |
| Channel | OPVkWall | ownerId | Id of user or community to post to. Should be positive or null. Null means to post on wall of a specified user |
| Channel | OPVkWall | community | Boolean flag to show that `ownerId` is actually id of a group or community |
| Channel | OPVkWall | userId | Identification (integer) of a user to be used for posting on vkontakte. Overrides `defaultUserId` |
| Channel | OPVkWall | userAcessToken | Access Token for user specified by id previously. Overrides `defaultUserAccessTonen` |

#### Read More

* [VKontakte Dev Home](https://vk.com/apps?act=manage)
* [Why wall authorization is too complex?](https://habr.com/ru/post/179953/)

### Instagram

| Nature | OClass | Property | Description |
|--------|--------|----------|-------------|
| Platform App | OPIGApp |       | OClass for association with Instagram. Last one doesn't allow to create your own application. So instance of this class should be created just nominally if you want to post to Instagram |
| Channel | OPIGAccount |         | OClass for defining details about Instagram account to post to |
| Channel | OPIGAccount | username | Instagram handle of a user to post on behalf of |
| Channel | OPIGAccount | password | Password of a user to post on behalf of |

### Twitter

| Nature | OClass | Property | Description |
|--------|--------|----------|-------------|
| Platform App | OPTwitterApp |       | OClass for specifying details about Twitter Application which needs to be created to post with OPoster |
| Platform App | OPTwitterApp | apiKey | Application Key which corresponds to application created in Twitter. Please check settings page of your app |
| Platform App | OPTwitterApp | apiSecretKey | Application Secret Key string which was defined/generated by Twitter for your application |
| Channel | OPTwitterAccount |         | OClass for defining details about twitter account to post on |
| Channel | OPTwitterAccount | accessToken | Access Token of a Twitter user to post on behalf of. Can be obtained by clicking button **Connect OAuth** |
| Channel | OPTwitterAccount | accessTokenSecret | Access Token Secret of a Twitter user to post on behalf of. Can be obtained by clicking button **Connect OAuth** |

#### Read More

* [Twitter Dev Home Page](https://developer.twitter.com/apps)

### Odnoklassniki

| Nature | OClass | Property | Description |
|--------|--------|----------|-------------|
| Platform App | OPOkApp |       | OClass for specifying details about Odnoklassniki Application which needs to be created to post with OPoster |
| Platform App | OPOkApp | appId | Application Identification which corresponds to application created in Odnoklassniki |
| Platform App | OPOkApp | publicKey | Public Key for the app in Odnoklassniki: check your email after app creation |
| Platform App | OPOkApp | secretKey | Secret Key for the app in Odnoklassniki: check your email after app creation |
| Channel | OPOkChannel |         | OClass for defining details about user, group/page in Odnoklassniki you are going to post on |
| Channel | OPOkChannel | groupId | Id of a group/page to post to. Should be positive or null. Null means to post on wall of a specified user |
| Channel | OPOkChannel | acessToken | Access Token for a user to post on behalf of |

#### Read More

* [Page to request dev access](https://ok.ru/devaccess)
* [List of your apps](https://ok.ru/vitrine/myuploaded)
* [How to create app in Odnoklassniki](https://apiok.ru/dev/app/create)
* [Odnoklassniki REST methods](https://apiok.ru/dev/methods/rest/)
Remember, that you have to work with support to get required rights for your application: **PUBLISH_TO_STREAM;VALUABLE_ACCESS;LONG_ACCESS_TOKEN;PHOTO_CONTENT;GROUP_CONTENT**

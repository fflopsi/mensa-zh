# MensaZH

The menus of all mensas of ETHZ and UZH in Zürich in one app.

<p align="center">
  <a href="https://play.google.com/store/apps/details?id=ch.famoser.mensa"><img src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png" alt="Get it on Google Play" height="80"></a>
  <a href="https://f-droid.org/packages/ch.famoser.mensa/"><img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" alt="Get it on F-Droid" height="80"></a>
</p>

*This app is heavily based on [@famoser's](https://github.com/famoser) work on
his [Mensa app](https://github.com/famoser/Mensa). I modernized and rewrote the app using Jetpack
Compose.*

| Light mode                                                                                    | Dark mode                                                                                  |
|-----------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------|
| ![Phone list view in light mode](./metadata/en-US/images/phoneScreenshots/list-light.png)     | ![Phone list view in dark mode](./metadata/en-US/images/phoneScreenshots/list-dark.png)    |
| ![Phone detail view in light mode](./metadata/en-US/images/phoneScreenshots/detail-light.png) | ![Phone list view in dark mode](./metadata/en-US/images/phoneScreenshots/detail-dark.png)  |
| ![Phone settings in light mode](./metadata/en-US/images/phoneScreenshots/settings-light.png)  | ![Phone settings in dark mode](./metadata/en-US/images/phoneScreenshots/settings-dark.png) |
| ![Tablet in light mode](./metadata/en-US/images/tenInchScreenshots/light.jpg)                 | ![Tablet in dark mode](./metadata/en-US/images/tenInchScreenshots/dark.jpg)                |

## Contributing

I'm grateful for any help! If you want to contribute, check out the more [detailed info on contributing](CONTRIBUTING.md).

---

## Release checklist

- [ ] Update version in code
  - [ ] Verify that the [GitHub build](https://github.com/fflopsi/mensa-zh/actions/workflows/android.yml) passes
  - [ ] Update `versionCode` and `versionName` in `app/build.gradle.kts`
  - [ ] Summarize changes in `metadata/[lang]/changelogs`
- [ ] Play Store update
  - [ ] Generate signed `abb` app bundle
  - [ ] Create a new release on Play Console  and attach the signed app bundle
- [ ] F-Droid/GitHub update
  - [ ] Generate signed `apk`
  - [ ] Create a new release on GitHub with the `versionName` (tag and title) and attach the signed `apk`

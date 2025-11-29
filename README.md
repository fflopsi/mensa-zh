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

## Update mensa details

Has a new mensa opened, or are the opening times no longer accurate? Feel free to directly submit
a [pull request](https://github.com/fflopsi/mensa-zh/compare)!

For ETH, look at [eth/locations.json](./app/src/main/assets/eth/locations.json). The `infoUrlSlug`
must match the hompage slug (e.g. `zentrum/clausiusbar` for
`https://ethz.ch/de/campus/erleben/gastronomie-und-einkaufen/gastronomie/restaurants-und-cafeterias/zentrum/clausiusbar.html`).
The `facilityId` must be the id of the menu plan (e.g. for Clausiusbar, the menu plan
linked [here](https://ethz.ch/de/campus/erleben/gastronomie-und-einkaufen/gastronomie/menueplaene.html)
has the URL
`https://ethz.ch/de/campus/erleben/gastronomie-und-einkaufen/gastronomie/menueplaene/offerDay.html?date=2025-11-07&id=3`).

For UZH, look at [uzh/locations_zfv.json](./app/src/main/assets/uzh/locations_zfv.json).The
`infoUrlSlug` must match the homepage slug (e.g. `raemi59` for
`https://www.mensa.uzh.ch/de/menueplaene/raemi59.html`). The `slug` must match the slug used in the
GraphQL endpoint of [ZFV](https://api.zfv.ch/graphql) (do a query on location and kitchen using
these values). Note that this API needs a key. There are other APIs (e.g.
[food2050](https://api.app.food2050.ch/)), but ZFV only needs one request.

For implementation details on how the links are constructed (to try it out yourself), check
out [ETHMensaProvider.kt](./app/src/main/java/ch/florianfrauenfelder/mensazh/services/providers/ETHMensaProvider.kt)
and [UZHMensaProvider.kt](./app/src/main/java/ch/florianfrauenfelder/mensazh/services/providers/UZHMensaProvider.kt).

## Development status

I have taken over development and maintenance of the Mensa app
from [@famoser](https://github.com/famoser). I will continue to provide updates for the Android
version of this app in the next few years. If you are interested in helping with the development or
even adding a iOS version
using [Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/), feel free to reach
out, create an [issue](https://github.com/fflopsi/mensa-zh/issues/new/choose) or submit
a [pull request](https://github.com/fflopsi/mensa-zh/compare).

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

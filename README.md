![logo](https://user-images.githubusercontent.com/1917608/36115982-09a33b84-1035-11e8-81cc-23e3de9fa808.png)
_WearTo lets you add items to Todoist with minimal effort using your wearable. Just like it should be._

[![google-play-badge](https://user-images.githubusercontent.com/1917608/36116679-614bba4e-1037-11e8-891f-9bd7bca10e97.png)
](https://play.google.com/store/apps/details?id=se.yverling.wearto)

## Building and running
The projects consits of two seperate Gradle projects
* `wear`  - the werable app 
* `mobile` - the phone/tablet app

Just point [Android Studio](https://developer.android.com/studio/index.html) to one of the project folders to build and run it.

### Debugging the `wear` app
If you want to debug the `wear` app on your wearable make sure to read the [Debugging an Android Wear App](https://developer.android.com/training/wearables/apps/debugging.html) article on how to set up bluetooh debugging.

### License and acknowledgments
This projects is gratefully using the following third party libraries:
* [Kotlin](https://github.com/JetBrains/kotlin/tree/master/license)
* [anko](https://github.com/Kotlin/anko/blob/master/LICENSE)
* [RxJava](https://github.com/ReactiveX/RxJava/blob/2.x/LICENSE)
* [Retrofit](https://github.com/square/retrofit/blob/master/LICENSE.txt)
* [Stetho](https://github.com/facebook/stetho/blob/master/LICENSE)
* [MaterialTapTargetPrompt](https://github.com/sjwall/MaterialTapTargetPrompt/blob/master/LICENSE)
---
* [JUnit](https://github.com/junit-team/junit4/blob/master/LICENSE-junit.txt)
* [assertk](https://github.com/willowtreeapps/assertk/blob/master/LICENSE)
* [Mockito](https://github.com/mockito/mockito/blob/release/2.x/LICENSE)
* [mockwebserver](https://github.com/square/okhttp/tree/master/mockwebserver#license)

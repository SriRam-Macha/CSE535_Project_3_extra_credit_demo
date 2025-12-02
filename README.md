# CSE535_Project_3_extra_credit_demo

Readme

Demo Description 

So the purpose of this demo is to show the some of the features which would be available to an application when they are using the zero trust SDK in this demo app(We have not implemented an SDK we have made an demo/pilot app to show features of an Zero Trust SDK), In an actual SDK we are trying to analyse user behaviour to calculate a trust score. This trust score determines the when and how strong security should be present at any given time in the app. This trust score would be calculated based on various metrics and calculations, such as -

* Text patterns
* Typing speed
* Mobile location
* accelerometer reading(demo does not use it)
* Capacitive touch behaviour (demo does not use it)

Various detection methods are used in the demo to calculate the trust score

Typing speed - key rocks are analysed to find unusual rhythms and calculate the Trustscore when a Trustscore drops significantly, the security status is elevated, and this disables only some of the features(in the demo, like masking the account balance  & requiring extra verification for money transfer)

Text patterns - a bot typing attack is simulated in the demo, so superhuman speed typing can be detected and again, which affects the trust score

Mobile location - jump simulation button is also added, when clicked, the location of the mobile device changes rapidly, which is usually not possible this again impacts the trust score

Accelerometer readings - These readings can be used to assist mobile location detection. GPS proofing(Accelerometer readings are only displayed in the demo)

Capacity touch behaviour - can be used to detect finger stroke patterns to determine if a human or a machine is trying to make the gestures on a mobile device


How to install the application 

Here are concise installation instructions for the ZeroTrustBankingDemo project:
Installation Instructions
Prerequisites
* Android Studio (latest stable version recommended)
* JDK 17 or higher
* Android SDK with minimum API level 24 (Android 7.0)
Steps
1. Clone/Download the Repository  git clone <repository-url>
2. cd ZeroTrustBankingDemo  
3. Open in Android Studio
    * Launch Android Studio
    * Select "Open an Existing Project"
    * Navigate to the downloaded ZeroTrustBankingDemo folder
    * Click "OK"
4. Sync Gradle
    * Android Studio will automatically prompt to sync Gradle
    * If not, click File → Sync Project with Gradle Files
    * Wait for dependencies to download
5. Run the App
    * Connect an Android device (USB debugging enabled) OR start an Android Emulator
    * Click the Run button (green play icon) or press Shift + F10
    * Select your device/emulator
    * Wait for build and installation
Demo Credentials
* Username: user
* Password: password
Key Features to Test
1. Login - Observe live behavioral analysis
2. Trust Score - Monitor real-time scoring (0-100)
3. Attack Simulations - Use "Bot Typing" and "Loc. Jump" buttons
4. Reset - Clear demo state and restore trust score
5. Transfer - Test adaptive security (Approve/Challenge/Block based on trust score)
That's it! The app should build and run without additional configuration.

Additional points - 
The trust score can sometimes misbehave even when typing normally for demo purposes. We tried to make it as lenient as possible; this is something which needs more data and complex algorithms to increase accuracy. 

Feature not included in the demo video due to time restriction -
When username and password is entered five times wrong, this again triggers really low Trust score, which again stops you from using all the features of the app.

Video link - https://www.youtube.com/watch?v=ZMYCQ7_9Je4 (demo 5:37 time)

Generative AI Acknowledgment: Portions of the code in this project were generated with assistance from ChatGPT, Perplexity Pro, and Claude
* Estimated percentage of code influenced by Generative AI: 17.3%


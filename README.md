# Proton Kotlin SDK

Kotlin library for handling Proton Chain operations. The main
purpose for this library is for account and key management, but also
handles signing and pushing transactions.

# Installation

First make sure you include jcenter() in your root build.gradle

```gradle
allprojects {
    repositories {
        ...
        jcenter()
    }
}
```

Then add the following dependency to your module's build.gradle

```gradle
dependencies {
    ...
    implementation "com.metallicus:protonsdk:0.4.5"
}
```

# Usage

The main class that you will interface with is `Proton` which
encapsulates all the needed functions.

To initialize make sure you use an appropriate MainNet or
TestNet chain provider url (normally during app startup).

```kotlin
override fun onCreate() {
    super.onCreate()
    
    Proton.getInstance(this).initialize(protonChainProviderUrl)
}
```

MainNet Chain Provider Url:
```
https://api-dev.protonchain.com/v1/chain/info
```

TestNet Chain Provider Url:
```
https://api-dev.protonchain.com/v1/chain/info
```

# Credits

EOS APIs inspired by
[EOS Commander](https://github.com/playerone-id/EosCommander)  
Key Storage inspired by
[Secure Device Storage](https://github.com/adorsys/secure-storage-android)

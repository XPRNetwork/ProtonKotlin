# Proton Kotlin Wallet SDK (Beta) 🚧

![Bintray](https://img.shields.io/bintray/v/protonprotocol/ProtonKotlin/com.metallicus.protonsdk)
![GitHub release (latest by date including pre-releases)](https://img.shields.io/github/v/release/ProtonProtocol/ProtonKotlin?include_prereleases)
![GitHub](https://img.shields.io/github/license/ProtonProtocol/ProtonKotlin)

Kotlin library for handling Proton Chain operations. The main purpose
for this library is for account and key management, but also handles
signing and pushing transactions.

**Important:** *This library is currently under heavy development.
Please be aware that all functionality is subject to change at anytime.
Documentation and examples are also being worked on and will be added
over time as well.*

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
    implementation "com.metallicus:protonsdk:0.8.3"
}
```

*The minimum Android SDK requirement is 21.*

# Usage

The main class that you will interface with is `Proton` which
encapsulates all the needed functions.

First you will need to initialize with either a MainNet or TestNet url.

MainNet:

```
https://api.protonchain.com
```

TestNet:

```
https://api-dev.protonchain.com
```

This is recommended during app startup.

```kotlin
override fun onCreate() {
    super.onCreate()
    
    Proton.getInstance(this).initialize(protonChainUrl)
}
```

Once initialized you will be able to:
- Fetch Chain Provider info
- Fetch Token Contracts
- Find Proton accounts given a public or private key
- Generate and/or securely store a private key
- Set and store an "active" account

Once an active account has been set you can:
- Fetch token balances (with exchange rates) for active account
- Fetch transaction history for active account
- Update account name and avatar
- Transfer tokens from active account to another account

Most of these functions require network connectivity and will return a
LiveData resource.  
This LiveData can be observed and will emit the current state of the
action.

Here is an example of fetching Chain Provider info with the appropriate
observable states:

```kotlin
Proton.getInstance(context).getChainProvider().observe(this, Observer { chainProviderResource ->
    when (chainProviderResource?.status) {
        Status.SUCCESS -> {
            // Handle success state
        }
        Status.ERROR -> {
            // Handle error state
        }
        Status.LOADING -> {
            // Handle loading state
        }
    }
})
```

You can follow this format for most of the functions called within
`Proton`.

# Credits

EOS APIs inspired by
[EOS Commander](https://github.com/playerone-id/EosCommander)  
Key Storage inspired by
[Secure Device Storage](https://github.com/adorsys/secure-storage-android)

# MIT License

Copyright (c) 2020 Proton Chain LLC, Delaware

Permission is hereby granted, free of charge, to any person obtaining a
copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be included
in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.


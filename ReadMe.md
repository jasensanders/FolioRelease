# Welcome to Folio

### This is the app that I originally submitted as my final project for Udacity's Android Development Nanodegree.
The app acts as a way to catalog all of your Blu-ray and DVD movies using only the upc code on the back.  
[ This app is also available on the play store ](https://play.google.com/store/apps/details?id=com.enrandomlabs.jasensanders.v1.folio)

### To build this version you will need 2 keys and some Ad IDs from AdMob:

1. A Key from The Movie Data Base which you can get here:
	https://www.themoviedb.org/account/signup  -and Sign up.
	Then Click on the "API" link from the left hand sidebar within your account page.
2. A key from Search UPC data base which you can get here:
	http://searchupc.com/developers/ -and Sign up.
	
3. A google API key which you can get here: [Google Books API](https://developers.google.com/books/docs/v1/getting_started)

Put all three of these keys in the strings.xml file under:

	<string name = "search_upc_key".....>
	    and
	<string name = "tmdb_key".....>
	    and
	<string name = "google_books_key"....>

4. You can use my Ads app IDs, but then I will get paid for them, you can make your own here:
[Admob Setup](https://www.google.com/admob/)


### In This Version (1.04):
* Google Assistant app search works! (Ok, google, search Folio for.....)
* Privacy Policy, Apache License, code style fixes.

### Version 1.03 features
* Manual Entry
* SearchView widget works
* Code is more google style compliant
* Refactored IntentServices to fail more gracefully **Easier to debug!**
* Memory leak fixes

### It looks like this:

![Main Activity](https://lh3.googleusercontent.com/X2Uwc1FFSY27w1HgAt6KGkFgvPCuc7ucByYGay7fAEz73BTPBZZ641dWAi4ZDw02FA4=h900-rw "Main Activity")

![Nav Drawer](https://lh3.googleusercontent.com/Yfbv_hSf2NfWSE8WNZJVz7qyylIyVqboDnTE_35HX64QIpcf49sXvVGFYgIq9SbvkBQ=h900-rw "Nav Drawer")

![Detail View](https://lh3.googleusercontent.com/o7ZIcxXjgAnMnKZFZliKLeIq-Xi9YH1HFXg0tMzbSBEgWaKFKMTrx2nniMN5aQy0dB8=h900-rw "Detail View")

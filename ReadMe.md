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

Put both of these keys in the strings.xml file under:
	<string name="search_upc_key".....>
	    and
	<string name="tmdb_key".....>

3. You can use my Ads app IDs, but then I will get paid for them, you can make your own here:
[Admob Setup](https://www.google.com/admob/)


### In This Version (1.03):
* Manual Entry
* SearchView widget works
* Code is more google style compliant
* Refactored IntentServices to fail more gracefully ####Easier to debug!
* Memeory leak fixes

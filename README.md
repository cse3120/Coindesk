# Coindesk
A command-line Java program that fetches data from the following public APIs:
https://api.coindesk.com/v1/bpi/historical/close.json?start=2013-09-01&end=2013-09-05&currency=eur
https://api.coindesk.com/v1/bpi/currentprice/eur.json
Once executed, the program should request the user to input a currency code (USD, EUR, GBP, etc.)
Once the user provides the currency code, the application should display the following information:
 The current Bitcoin rate, in the requested currency
 The lowest Bitcoin rate in the last 30 days, in the requested currency
 The highest Bitcoin rate in the last 30 days, in the requested currency
If the currency code provided is not supported by the API, a message should inform the user.

function getQueryVariable(variable) {
  var query = window.location.search.substring(1);
  var vars = query.split("&");
  for (var i=0;i<vars.length;i++) {
    var pair = vars[i].split("=");
    if (pair[0] == variable) {
      return pair[1];
    }
  }
}

function timestampToYYYYMMDD(timestamp) {
  let month = '' + (timestamp.getMonth() + 1);
  let day = '' + timestamp.getDate();
  let year = timestamp.getFullYear();

  if (month.length < 2) {
    month = '0' + month;
  }
  if (day.length < 2) {
    day = '0' + day;
  }

  return [year, month, day].join('-');
}

const ticker = getQueryVariable("ticker");;

const date = new Date();
date.setFullYear(date.getFullYear() - 2);
const fromDate = timestampToYYYYMMDD(date);
console.log(fromDate);

/* const url = "https://stock-search-backend-110320.wl.r.appspot.com/search/prices/amzn?startDate="
+ fromDate
+ "&resampleFreq=12hour" */

const url = `https://stock-search-backend-110320.wl.r.appspot.com/search/prices/${ticker}?startDate=${fromDate}&resampleFreq=12hour`

console.log(url);


Highcharts.getJSON(url, function (data) {

    // split the data set into ohlc and volume
    var ohlc = [],
        volume = [],
        dataLength = data.length,
        // set the allowed units for data grouping
        groupingUnits = [[
            'week',                         // unit name
            [1]                             // allowed multiples
        ], [
            'month',
            [1, 2, 3, 4, 6]
        ]],

        i = 0;

    for (i; i < dataLength; i += 1) {
    		let date = new Date(data[i].date.substring(0, 10));
        let timestamp = Date.parse(date.toString());
        ohlc.push([
        		timestamp,
            data[i].open,
            data[i].high,
            data[i].low,
            data[i].close
        ]);

        volume.push([
            timestamp,
            data[i].volume // the volume
        ]);
    }


    // create the chart
    Highcharts.stockChart('container', {

        rangeSelector: {
            selected: 2
        },

        title: false,

        subtitle: false,

        yAxis: [{
            startOnTick: false,
            endOnTick: false,
            labels: {
                align: 'right',
                x: -3
            },
            title: {
                text: 'OHLC'
            },
            height: '60%',
            lineWidth: 2,
            resize: {
                enabled: true
            }
        }, {
            labels: {
                align: 'right',
                x: -3
            },
            title: {
                text: 'Volume'
            },
            top: '65%',
            height: '35%',
            offset: 0,
            lineWidth: 2
        }],

        tooltip: {
        		valueDecimals: 2,
            split: true
        },

        plotOptions: {
            series: {
                dataGrouping: {
                   units: groupingUnits
                }
            }
        },

        series: [{
            type: 'candlestick',
            name: `${ticker}`,
            id: 'upper',
            zIndex: 2,
            data: ohlc
        }, {
            type: 'column',
            name: 'Volume',
            id: 'volume',
            data: volume,
            yAxis: 1
        }, {
            type: 'vbp',
            linkedTo: 'upper',
            params: {
                volumeSeriesID: 'volume'
            },
            dataLabels: {
                enabled: false
            },
            zoneLines: {
                enabled: false
            }
        }, {
            type: 'sma',
            linkedTo: 'upper',
            zIndex: 1,
            marker: {
                enabled: false
            }
        }]
    });
});
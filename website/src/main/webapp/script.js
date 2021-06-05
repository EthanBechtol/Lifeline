// Created with the help of https://developers.google.com/chart/interactive/docs/quick_start
google.charts.load('current', {'packages':['corechart']});
google.charts.setOnLoadCallback(drawChart);

function drawChart() {
  fetch('/fetch-vitals').then(response => response.json())
  .then((vitalData) => {
    const data = new google.visualization.DataTable();
    data.addColumn('date', 'Time');
    data.addColumn('number', 'Oxygen Saturation');
    data.addColumn('number', 'Heartrate');
    vitalData.forEach(entry => {
        data.addRow([new Date(entry["timestamp"]), entry["o2"], entry["hr"]]);
    });

    const options = {
      'title': 'Vital Reading History',
      'width':1000,
      'height':500,
    //    colors: ['#8b0000', '#00008b'],
    //   trendlines: {
    //     0: {type: 'linear', opacity: 0.5},
    //     1: {type: 'linear', opacity: 0.5}
    //   },
      crosshair: { trigger: 'both', orientation: 'vertical' },
      curveType: 'function',
    };

    const chart = new google.visualization.LineChart(document.getElementById('vital-chart'));
    chart.draw(data, options);
  });
}


function convertTimestampToDateTime(timestamp) {
    const dateString = new Date(timestamp).toLocaleDateString("en-US");
    const timeString = new Date(timestamp).toLocaleTimeString("en-US");
    return dateString + " " + timeString;
}

function generateNewEventRow(id, timestamp, type, description) {
    let event = "        <td>%TIME%</td>\n" +
        "                <td>%TYPE%</td>\n" +
        "                <td>%DESCRIPTION%</td>\n"

    event = event.replace("%TIME%", convertTimestampToDateTime(timestamp))
        .replace("%TYPE%", type)
        .replace("%DESCRIPTION%", description)

    return event;
}

async function updateEvents() {
    let table = document.createElement("table");
    table.innerHTML = "                <colgroup>\n" +
        "                    <col style=\"width:15%\">\n" +
        "                    <col style=\"width:10%\">\n" +
        "                    <col style=\"width:75%\">\n" +
        "                </colgroup>\n" +
        "                <tr>\n" +
        "                    <th>Time</th>\n" +
        "                    <th>Event Type</th>\n" +
        "                    <th>Description</th>\n" +
        "                </tr>"
    let eventCount = 0;
    await fetch('/fetch-events').then(response => response.json()).then((eventsResponse) => {
        eventsResponse.forEach((event) => {
            let newRow = table.insertRow();
            newRow.innerHTML = generateNewEventRow(event["id"], event["timestamp"], event["type"], event["description"]);
            if(event["type"] == "VITAL") {
                newRow.style.color = "red";
            }
            ++eventCount;
        })
    });
    document.getElementById('event-table').innerHTML = "";
    document.getElementById('event-table').appendChild(table);
    document.getElementById('event-count').innerText = eventCount + "";
}

async function updateVitals() {
    let hr = 0;
    let o2 = 0;
    let orientation = "?";
    let timestamp = 0;
    await fetch('/fetch-vitals?limit=1').then(response => response.json()).then((vitalsResponse) => {
        vitalsResponse.forEach((vital) => {
            hr = vital["hr"];
            o2 = vital["o2"];
            orientation = vital["orientation"];
            timestamp = vital["timestamp"];
        })
    });
    document.getElementById('live-readings-last-update').innerText = timeSince(new Date(timestamp));
    document.getElementById('hr').innerText = hr + 'bpm';
    document.getElementById('o2').innerText = o2 + '%';
    document.getElementById('orientation').innerText = orientation;

    if(hr < 60 || hr >= 90){
        document.getElementById('hr').style.color = "red";
    }
    else {
        document.getElementById('hr').style.color = "green";
    }

    if(o2 < 96) {
        document.getElementById('o2').style.color = "red";
    }
    else {
        document.getElementById('o2').style.color = "green";
    }
}

async function updateAll() {
    updateEvents();
    updateVitals();
}

// Adapted from: https://stackoverflow.com/questions/6108819/javascript-timestamp-to-relative-time
function timeSince(timeStamp) {
    var sDiff = (new Date().getTime() - timeStamp.getTime() ) / 1000;
    if(sDiff < 60){
        return Math.round(sDiff) + ' second(s) ago';
    }
    else if(sDiff < 3600){
        return Math.round(sDiff/60) + ' minute(s) ago';
    }
    else if(sDiff <= 86400){
        return Math.round(sDiff/3600) + ' hour(s) ago';
    }
    else {
        return timeStamp.toDateString()
    }
}

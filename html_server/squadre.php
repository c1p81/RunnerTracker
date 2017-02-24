<!DOCTYPE html>
<?php
$squadra = $_GET['squadra'];
//print $squadra;
?>
<html lang="it">
	<head>
		<meta charset="utf-8">
		<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
		<meta http-equiv="refresh" content="60">
		<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">

      <!-- Leaflet 0.5: https://github.com/CloudMade/Leaflet-->
		<link rel="stylesheet" href="./lib/leaflet.css" />
		<!--[if lte IE 8]> <link rel="stylesheet" href="../../lib/leaflet.ie.css" />  <![endif]-->  
		<script src="./lib/leaflet.js"></script>

		<!-- MarkerCluster https://github.com/danzel/Leaflet.markercluster -->
		<link rel="stylesheet" href="./lib/MarkerCluster.css" />
		<link rel="stylesheet" href="./lib/MarkerCluster.Default.css" />
		<!--[if lte IE 8]> <link rel="stylesheet" href="../../lib/MarkerCluster.Default.ie.css" /> <![endif]-->
		<script src="./lib/leaflet.markercluster-src.js"></script>

		<!-- GeoCSV: https://github.com/joker-x/Leaflet.geoCSV -->
		<script src="./leaflet.geocsv-src.js"></script>

		<!-- jQuery 1.8.3: http://jquery.com/ -->
		<script src="./lib/jquery.js"></script>

		<style>	
		html, body, #mapa {
			margin: 0;
			padding: 0;
			width: 100%;
			height: 100%;	
			font-family: Arial, sans-serif;
			font-color: #38383;
		}

		#botonera {
			position:fixed;
			top:10px;
			left:50px;
			z-index: 2;
		}

		#cargando {
			position:fixed;
			top:0;
			left:0;
			width:100%;
			height:100%;
			background-color:#666;
			color:#fff;
			font-size:2em;
			padding:20% 40%;
			z-index:10;
		}

		.boton {
			border: 1px solid #96d1f8;
			background: #65a9d7;
			background: -webkit-gradient(linear, left top, left bottom, from(#3e779d), to(#65a9d7));
			background: -webkit-linear-gradient(top, #3e779d, #65a9d7);
			background: -moz-linear-gradient(top, #3e779d, #65a9d7);
			background: -ms-linear-gradient(top, #3e779d, #65a9d7);
			background: -o-linear-gradient(top, #3e779d, #65a9d7);
			padding: 12px 24px;
			-webkit-border-radius: 10px;
			-moz-border-radius: 10px;
			border-radius: 10px;
			-webkit-box-shadow: rgba(0,0,0,1) 0 1px 0;
			-moz-box-shadow: rgba(0,0,0,1) 0 1px 0;
			box-shadow: rgba(0,0,0,1) 0 1px 0;
			text-shadow: rgba(0,0,0,.4) 0 1px 0;
			color: white;
			font-size: 17px;
			/*font-family: Helvetica, Arial, Sans-Serif;*/
			text-decoration: none;
			vertical-align: middle;
		}
		.boton:hover {
			border-top-color: #28597a;
			background: #28597a;
			color: #ccc;
		}
		.boton:active {
			border-top-color: #1b435e;
			background: #1b435e;
		}
		</style>
	</head>
	<body>
		<div id="mapa"></div>
		<div id="cargando">Loading</div>

		<!--<div id="botonera">
			<button id="localizame" class="boton">Bankias cercanos</button>
		</div>-->

<script>

//;$(function() {

var mapa = L.map('mapa', {attributionControl:false}).setView([40.46, -3.75], 5);

L.tileLayer('http://tile.openstreetmap.org/{z}/{x}/{y}.png', {
	maxZoom: 18
}).addTo(mapa);

var bankias = L.geoCsv(null, {
	onEachFeature: function (feature, layer) {
		var popup = '';
		for (var clave in feature.properties) {
			var title = bankias.getPropertyTitle(clave);
			popup += '<b>'+title+' </b>: '+feature.properties[clave]+'<br />';
		}
		layer.bindPopup(popup);
	},
	pointToLayer: function (feature, latlng) {
		return L.marker(latlng, {
			icon:L.icon({
				iconUrl: feature.properties['icon'],
        		shadowUrl: 'shadow.png',
        		iconSize: [25,41],
        		shadowSize:   [41, 41],
        		shadowAnchor: [13, 20]
			})
		});
	},
	firstLineTitles: true
});

$.ajax ({
	type:'GET',
	dataType:'text',
	url:'<?print $squadra?>.txt',
   error: function() {
     alert('Dati non disponibili');
   },
	success: function(csv) {
      var cluster = new L.MarkerClusterGroup();
		bankias.addData(csv);
		cluster.addLayer(bankias);
		mapa.addLayer(cluster);
		mapa.fitBounds(cluster.getBounds());
	},
   complete: function() {
      $('#cargando').delay(500).fadeOut('slow');
   }
});


$('#localizame').click(function(e) {
	mapa.locate();
	$('#localizame').text('Localizando...');
	mapa.on('locationfound', function(e) { 
		mapa.setView(e.latlng, 15);
		$('#localizame').text('Bankias cercanos');
	});
});

//});
</script>

	</body>
</html>


<?
phpinfo();
?>
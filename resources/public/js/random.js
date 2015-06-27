document.addEventListener('DOMContentLoaded', function() {
	function getRandomInt(min, max) {
		return Math.floor(Math.random() * (max - min)) + min;
	}

	var selectBox = $('select[name=curry]');
	var options = selectBox.children();
	selectBox.css("border", "1px lightgrey solid");

	window.pickRandomCurry = function() {
		var end = getRandomInt(0, options.length - 1);
		var recur = function(iterations){
			var nextOption = (end + iterations) % options.length;
			options[nextOption].selected = true;
			var timeout = 2000 / Math.pow(iterations, 1.25);

			// Debug
			//console.log("iterations " + iterations + ", next timeout: " + timeout);

			if(iterations <= 1) {
				selectBox.css("border", "1px green solid");
				window.displayPrice();
				return;
			} else {
				setTimeout(function() {
					recur(iterations - 1);
				}, timeout);
			}
		};
		recur(getRandomInt(50, 50 + options.length));
	}

	$('#random-curry').click(function(event) {
		event.preventDefault();
		window.pickRandomCurry();
	});
}, false);

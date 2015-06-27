document.addEventListener('DOMContentLoaded', function(){
	window.displayPrice = function(){
		var veggie = $('select[name=curry] option:selected').attr('x-veggie') === 'true';
		var garlic = $('input[name=garlic]:checked').val() === 'true';

		var price = (veggie ? 10 : 11);
		price += (garlic ? 1 : 0);

		$('input[type=submit]').val("Order ($" + price + ")");
	};
	window.displayPrice();
	$('input, select').change(window.displayPrice);
}, false);

document.addEventListener('DOMContentLoaded', function(){
	var displayPrice = function(){
		var veggie = $('select[name=curry] option:selected').attr('x-veggie') === 'true';
		var garlic = $('input[name=garlic]:checked').val() === 'true';

		var price = (veggie ? 10 : 11);
		price += (garlic ? 1 : 0);

		$('input[type=submit]').val("Order ($" + price + ")");
	};
	displayPrice();
	$('input, select').change(displayPrice);
}, false);

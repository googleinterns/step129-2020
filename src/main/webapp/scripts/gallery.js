// Played around with the following packages: masonry.pkgd.js, imagesloaded.pkgd.js
// May need to use different libraries due to commercial use requirement
// Masonry is released under the MIT license: https://desandro.mit-license.org/

// global variables
var filters = []
var $active_filters_ul = $('.active-filters');
var $grid = $('.grid')
var num_filters = 0;

// Call these functions when page loads
$(document).ready(function() {
  // Initialize Masonry
  console.log("initializing masonry");
  $grid.masonry({
    itemSelector: '.grid-item',
    percentPosition: true,
    // horizontalOrder: true,
    columnWidth: '.grid-sizer'
  });

  // get all data
  fetchAllSpeciesData();

  // hides filters on click
  deleteFilter()
});

// Test function that fetches sample JSON and appends each species to the gallery
function fetchAllSpeciesData() {
    fetch("/allData").then(response => response.json()).then(speciesData => {
        for (var species in speciesData) {
            // Append images to grid
            var $html = $(
                '<div class="grid-filters ' + speciesData[species].status + ' ' + speciesData[species].trend + ' ' + speciesData[species].taxonomicPath.order_t + ' ' + speciesData[species].taxonomicPath.class_t + '">' +
                  '<div class="grid-sizer"></div>' +
                  '<div class="grid-item">' +
                    '<img src="'+ speciesData[species].imageLink +'" />' +
                    '<div class="overlay">' + 
                      '<a href="/species-template.html?species=' + speciesData[species].commonName + '"> ' + speciesData[species].commonName + '</a>' +
                    '</div> ' +
                  '</div>' +
                '</div>'); 

            $grid.append($html)
                // add and lay out newly appended items
                .masonry('appended', $html);
        }

        // Layout Masonry after each image finishes loading
        $grid.imagesLoaded().progress( function() {
            $grid.masonry('layout');
        });
    });
}


function showClass(class_name) {
  $('.' + class_name).show();
}

function showAllClasses() {
  $('.grid-filters').show();
}

function hideClass(class_name) {
  $('.' + class_name).hide();
}

function hideAllClasses() {
  $('.grid-filters').hide();
}

// function filterSelection(class_name) {
//   if (class_name === "all") {
//     showAllClasses();
//   } else {
//     hideAllClasses();
//     showClass(class_name);
//   }

//   // Update the masonry layout
//   $grid.masonry();
// }

function addFilter(class_name) {
  console.log(class_name)
  filters.push(class_name);
  $filter = $(
    '<li class="active-filter list-inline-item">' +
      '<button class="btn my-2 my-sm-0" type="submit">' +
        '<i class="fa fa-close"></i>' +
        class_name +
      '</button>' +
    '</li>'
  );
  $active_filters_ul.append($filter);

  if (num_filters == 0) {
    hideAllClasses();
  }
  showClass(class_name);
  num_filters++;
  $grid.masonry();
}

function deleteFilter() {
  $('.active-filters').on('click', 'button', function(){
    $(this).closest('li').remove();
    console.log($(this).text())
    hideClass($(this).text())
    num_filters--;
    if (num_filters == 0) {
      showAllClasses();
    }
    $grid.masonry();
  });
}

function clearFilters() {
  $('.active-filters').empty();
  showAllClasses();
  num_filters = 0;  
  $grid.masonry();
}
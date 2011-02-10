(function ($) {

    function get_id(element) {
        return $(element).attr('id') + '_ckeditor';
    }

    function remove_ck(id) {
    	try {
	    	var editor = CKEDITOR.instances[id];
	    	if (editor) {
	    		try {
	    			editor.destroy();
	    		} catch (de) {}
		        CKEDITOR.remove(editor);
		    	delete editor;
	    	}
    	} catch (e) {};
    }

    $.editable.addInputType('ckeditor', {
        element : function(settings, original) {
        	var id = get_id(original);
            var textarea = $('<textarea id="' + id + '"/>');
            if (settings.rows) {
                textarea.attr('rows', settings.rows);
            } else {
                textarea.height(settings.height);
            }
            if (settings.cols) {
                textarea.attr('cols', settings.cols);
            } else {
                textarea.width(settings.width);
            }
            $(this).append(textarea);
            return(textarea);
        },
        plugin : function(settings, original) {
            var id = get_id(original);
            remove_ck(id);
            var ckeditorType = settings.ckeditorType || 'User';
            var ckeditorConfig = {toolbar: ckeditorType};
            if (ckeditorType == 'Mini') {
            	ckeditorConfig = $.extend(ckeditorConfig, {filebrowserBrowseUrl: null, filebrowserFlashBrowseUrl: null, filebrowserImageBrowseUrl: null, filebrowserLinkBrowseUrl: null});
            }
            if (settings.ckeditorConfig) {
            	ckeditorConfig = $.extend(ckeditorConfig, settings.ckeditorConfig);
            }
            var ckeditorInstance=CKEDITOR.replace(id, ckeditorConfig);
            if (typeof wcagCompliant == 'function') {
            	ckeditorInstance.checkWCAGCompliance=wcagCompliant;
            }
        },
        submit : function(settings, original) {
            var id = get_id(original);
            $('#' + id).val(CKEDITOR.instances[id].getData());
            remove_ck(id);
        },
        reset : function(settings, original) {
            var id = get_id(original);
            original.reset();
            remove_ck(id);
        }
    });
})(jQuery);

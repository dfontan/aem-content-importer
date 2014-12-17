<%@page import="com.adobe.aem.importer.DITATransformerHelper"%>
<%@page import="com.day.cq.i18n.I18n"%>
<%@page import="java.util.HashMap"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
<%@include file="/libs/foundation/global.jsp"%>

<%
 	String urlValidation = request.getRequestURL().toString();
 	urlValidation = urlValidation.replace(".html", "");
%>

<article class="learn-support-page">

	<section class="learn-section">
		<div class="learn">
			<img src="/etc/designs/mac-help/img/learn-icon.png" class="top-image" />
			<h1 class="features-title">Marketing Cloud Help and
				Documentation</h1>
			<div class="text parbase learn-description">
				<p>Get started uploading new content in website.</p>
				<br />
			</div>
			<form id="uploadContent" style="" action="<%=currentNode.getPath()%>"
				method="POST" enctype="multipart/form-data">
				<div class="serp-search">

					<label for="fileselect">File to upload data:</label> <input
						type="file" id="fileselect" name="fileselect" />
					<div id="filedrag">or drop files here</div>
					<div style="clear: both"></div>
					<button id="clearFile" type="button">Clear</button>
					<div id="messages" style="display: none;">
					</div>
				</div>

				<div class="serp-search custom-params" style="margin-top: 15px">
				<label for="src">Src</label> <input class="serp-search-input"
						id="src" name="src"
						placeholder="Path where pick up files to transform"
						autocomplete="off" type="text" />
					<label for="transformer">Transformer to apply</label> <select
						class="serp-search-input" name="transformer" id="transformer">
						<option value="">Empty</option>
						<% 
						  Class<?>[] availableTransformers = DITATransformerHelper.getAvailableTransformers();
							for(Class<?> transformer : availableTransformers) {
						%>
						<option value="<%=transformer.getName()%>"><%=transformer.getSimpleName().replace("Impl", "")%></option>

						<% } %>
					</select> 
					
					<label for="masterFile">Master
						file</label> <input class="serp-search-input" name="masterFile" id="masterFile"
						placeholder="..." autocomplete="off" type="text" />
					<label for="target">Target
						of results</label> <input class="serp-search-input" name="target"
						id="target" placeholder="Path where store the result"
						autocomplete="off" type="text" /> 
					<label for=customProps>Custom properties</label> 
					<textarea rows="3" class="serp-search-input" name="customProps"
						id="customProps" placeholder="Custom properties"></textarea>
				</div>

				<input id="execute" type="submit" class="button" style="margin-top: 15px" value="Execute"/>
				<progress id="progress" min="0" max="100" value="0" style="display: none;">0</progress>
				<div id="error" style="display: none"></div>
				<div id="success" style="display: none"></div>
			</form>


		</div>

		<div class="raw-html rawhtml"></div>
	</section>



</article>



<link rel="stylesheet"
	href="/etc/designs/mac-help/clientlibs_base/css/ccl_content.css" />
<style>
.header-container {
	height: 60px !important;
}

.serp-search:before {
	background: none !important;
}

.serp-search-input {
	padding-bottom: 0px !important;
	font-size: 1rem;
	padding-bottom: 15px !important;
	padding-left: 40px !important;
	padding-top: 15px !important;
}

div.serp-search {
	padding: 15px !important;
}

div.serp-search label {
	padding-left: 40px;
	font-size: 1.2rem;
}

.custom-params {
	text-align: left !important;
}

#filedrag {
	border: 2px dashed #555;
	border-radius: 7px;
	color: #555;
	cursor: default;
	font-weight: bold;
	margin: 1em 0;
	padding: 1em 0;
	text-align: center;
}

#messages {
	border: 1px solid #999;
	margin: 1em 0;
	padding: 0 10px;
}

 #messages p {
    font-size: 1.1rem;
}

#progress {
	width: 20%;
	margin-bottom: 10px;
	margin-left: auto;
	margin-right: auto;
}

#error {
	border: 1px solid red;
	background: none repeat scroll 0 0 #f75464;
}

#success {
	border: 1px solid green;
	background: none repeat scroll 0 0 #4BD446;
}

#error, #success {
	font-size: 1.2rem;
	color: white;
	width: 80%;
	margin-bottom: 15px;
	margin-left: auto;
	margin-right: auto;
}

#execute:disabled {
	background-color: grey;
}
</style>

<script>

	$("#execute").removeAttr("disabled");

	var formData = new FormData();
	var docToUpload = false;

	$("#execute").click(function() {
		$("#error").css("display","none");
		$("#success").css("display","none");
		$(this).html("Executing...");
		$(this).attr("disabled","disabled");
		$("#progress").css("display","block");
		submitForm();

		return false;
    });

    $("#clearFile").click(function() {
        $("#fileselect").val("");
        $("#messages").css("display","none");
        $("#messages").html("");

		$("#error").css("display","none");
        $("#error").html("");

        $("#success").css("display","none");
        $("#success").html("");

        $("#execute").removeAttr("disabled");
        
        $("#progress").css("display","none");

        formData = new FormData();
    });

	
	function submitForm() {
		
		// now post a new XHR request
		var xhr = new XMLHttpRequest();
		xhr.open('POST', '<%=currentNode.getPath()%>');

		formData.append("transformer", $("#transformer").val());
		formData.append("src", $("#src").val());
		formData.append("target", $("#target").val());
		formData.append("masterFile", $("#masterFile").val());
		formData.append("customProps", $("#customProps").val());

		xhr.onreadystatechange = function() {
			if (xhr.readyState == 4 && xhr.status == 200) {
				var data = $.parseJSON(xhr.responseText);
				var result = data['error'];
				console.log('result=', result);

				if (result == 'true') {
					$("#error").html("Process failed! Contact with the administrator");
					$("#error").css("display","block");
					$("#success").css("display","none");
					$("#execute").html("Execute");
					$("#execute").removeAttr("disabled");
					$("#progress").css("display","none");
					
				} else {
					console.log('successfully uploaded file');
                    $("#success").html("Sent it the information correctly. Workflow is going to lauch.");
                    $("#success").css("display","block");
                    
                    $("#execute").html("Execute");
					$("#execute").removeAttr("disabled");
	                 /*$("#execute").html("Reloading page...");
	                 
	                 setTimeout(function() {
	                	 location.reload(true);
	               }, 2000);*/
				}
				
			}
		}
		
		xhr.upload.onprogress = function (event) {
			  if (event.lengthComputable) {
			    var complete = (event.loaded / event.total * 100 | 0);
			    progress.value = progress.innerHTML = complete;
			  }
			};

			xhr.onload = function () {
			  // just in case we get stuck around 99%
			  progress.value = progress.innerHTML = 100;
			};

		xhr.send(formData);
	}

	/*
	 File Drag & Drop
	 */
	(function() {

		// getElementById
		function $id(id) {
			return document.getElementById(id);
		}

		// file drag hover
		function FileDragHover(e) {
			e.stopPropagation();
			e.preventDefault();
			e.target.className = (e.type == "dragover" ? "hover" : "");
		}

		// file selection
		function FileSelectHandler(e) {

			// cancel event and hover styling
			FileDragHover(e);

			// fetch FileList object
			var files = e.target.files || e.dataTransfer.files;
			
			formData = new FormData();
			formData.append('file',files[0]);
			ParseFile(files[0]);
		}

		// output file information
		function ParseFile(file) {
             if ("application/zip" != file.type) {
                $("#error").css("display","block").html("The file don't seem to be a zip. Do you want to continue?");
                 $("#messages").css("display","none");
                 $("#success").css("display","none");
             } else {
	             $("#error").css("display","none");
             }

             var m = $id("messages");
		     m.innerHTML = "<p>File added: <strong>" + file.name + "</strong> ";
             $("#messages").css("display","block");
             $("#success").css("display","none");

             docToUpload = true;


		}

		// initialize
		function Init() {

			var fileselect = $id("fileselect");
			var filedrag = $id("filedrag");

			// file select
			fileselect.addEventListener("change", FileSelectHandler, false);

			// is XHR2 available?
			var xhr = new XMLHttpRequest();
			if (xhr.upload) {
				// file drop
				filedrag.addEventListener("dragover", FileDragHover, false);
				filedrag.addEventListener("dragleave", FileDragHover, false);
				filedrag.addEventListener("drop", FileSelectHandler, false);
				filedrag.style.display = "block";

			}

		}

		// call initialization file
		if (window.File && window.FileList && window.FileReader) {
			Init();
		}

	})();
	
</script>

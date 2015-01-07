<%-- 
	Copyright (c) 2014 Adobe Systems Incorporated. All rights reserved.
	Licensed under the Apache License 2.0.
 	http://www.apache.org/licenses/LICENSE-2.0
 --%>
<%@page import="com.adobe.aem.importer.XMLTransformerHelper"%>
<%@page import="com.day.cq.i18n.I18n"%>
<%@page import="java.util.HashMap"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@include file="/libs/foundation/global.jsp"%>
<%@taglib prefix="cq" uri="http://www.day.com/taglibs/cq/1.0"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<script type="text/javascript">
	if (typeof jQuery === "undefined") { 
		var div = document.createElement("div");
		div.innerHTML = "jQuery library is a mandatory to be added in page to work this component";
		document.body.appendChild(div);
	} 
</script>


<div>
	<form id="uploadContent" style="" action="<%=resource.getPath()%>">
		<div class="title section cq-element-par_47title">
			<h3>Zip upload section</h3>
			<label for="fileselect">File to upload data:</label> <input
				type="file" id="fileselect" name="fileselect" />
			<div id="filedrag">or drop files here</div>
			<div style="clear: both"></div>
			<button id="clearFile" type="button">Clear</button>
			<div id="messages" style="display: none;"></div>
		</div>
		<div class="title section cq-element-par_47title">
			<h3>Set some params manually</h3>
			<div class="form_leftcol">
				<div class="form_leftcollabel">
					<label for="src">Src</label>
				</div>
				<div class="form_leftcolmark"></div>
			</div>
			<div id="email_rightcol" class="form_rightcol">
				<div id="email_0_wrapper" class="form_rightcol_wrapper">
					<input class="form_field form_field_text" id="src" name="src"
				placeholder="Path where pick up files to transform"
				autocomplete="off" type="text" />
				</div>
			</div>
			<div class="form_leftcol">
				<div class="form_leftcollabel">
					<label for="transformer">Transformer
				to apply</label>
				</div>
				<div class="form_leftcolmark"></div>
			</div>
			<div id="email_rightcol" class="form_rightcol">
				<div id="email_0_wrapper" class="form_rightcol_wrapper">
					<select class="form_field form_field_select"
				name="transformer" id="transformer">
				<option value="">Empty</option>
				<%
					Class<?>[] availableTransformers = XMLTransformerHelper.getAvailableTransformers();
														for(Class<?> transformer : availableTransformers) {
				%>
				<option value="<%=transformer.getName()%>"><%=transformer.getSimpleName().replace("Impl", "")%></option>

				<%
					}
				%>
			</select>
				</div>
			</div>
			
			<div class="form_leftcol">
				<div class="form_leftcollabel">
					<label for="masterFile">Master file</label>
				</div>
				<div class="form_leftcolmark"></div>
			</div>
			<div id="email_rightcol" class="form_rightcol">
				<div id="email_0_wrapper" class="form_rightcol_wrapper">
					<input
				class="form_field form_field_text" name="masterFile" id="masterFile"
				placeholder="..." autocomplete="off" type="text" />
				</div>
			</div>
			
			<div class="form_leftcol">
				<div class="form_leftcollabel">
					<label
				for="target">Target of results</label>
				</div>
				<div class="form_leftcolmark"></div>
			</div>
			<div id="email_rightcol" class="form_rightcol">
				<div id="email_0_wrapper" class="form_rightcol_wrapper">
					<input
				class="form_field form_field_text" name="target" id="target"
				placeholder="Path where store the result" autocomplete="off"
				type="text" />
				</div>
			</div>
			
			<div class="form_leftcol">
				<div class="form_leftcollabel">
					<label for=customProps>Custom properties</label>
				</div>
				<div class="form_leftcolmark"></div>
			</div>
			<div id="email_rightcol" class="form_rightcol">
				<div id="email_0_wrapper" class="form_rightcol_wrapper">
					<textarea rows="3" class="form_field form_field_textarea"
				name="customProps" id="customProps" placeholder="Custom properties"></textarea>
				</div>
			</div>
			
			       
			
		</div>
		<div id="error" style="display: none"></div>
		<div id="success" style="display: none"></div>
		<input id="execute" type="submit" class="button"
			style="margin-top: 15px" value="Execute" />
		<progress id="progress" min="0" max="100" value="0"
			style="display: none;">0</progress>
	</form>
</div>





<style>

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
	font-size: 0.8rem;
}

#progress {
	width: 20%;
	margin-bottom: 10px;
	margin-left: auto;
	margin-right: auto;
}

#error {
	color: red;
}

#success {
	color: green;
}

#error, #success {
	font-size: 0.8rem;
	margin-top: 15px;
	margin-bottom: 15px;
	font-weight: bold;
}

</style>

<script>

	$("#execute").removeAttr("disabled");

	var formData = new FormData();
	var docToUpload = false;

	$("#execute").click(function() {
		$("#error").css("display","none");
		$("#success").css("display","none");
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
		xhr.open('POST', '<%=resource.getPath()%>');

		formData.append("transformer", $("#transformer").val());
		formData.append("src", $("#src").val());
		formData.append("target", $("#target").val());
		formData.append("masterFile", $("#masterFile").val());
		formData.append("customProps", $("#customProps").val());

		xhr.onreadystatechange = function() {
			if (xhr.readyState == 4 && xhr.status == 200) {
				var data = $.parseJSON(xhr.responseText);
				var result = data['error'];
				if (result == 'true') {
					$("#error").html(
							"Process failed! Contact with the administrator");
					$("#error").css("display", "block");
					$("#success").css("display", "none");
					$("#execute").removeAttr("disabled");
					$("#progress").css("display", "none");

				} else {
					$("#success")
							.html(
									"Sent it the information correctly. Workflow is about to lauch.");
					$("#success").css("display", "block");
					$("#execute").remove();
				}

			}
		}

		xhr.upload.onprogress = function(event) {
			if (event.lengthComputable) {
				var complete = (event.loaded / event.total * 100 | 0);
				progress.value = progress.innerHTML = complete;
			}
		};

		xhr.onload = function() {
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
			formData.append('file', files[0]);
			ParseFile(files[0]);
		}

		// output file information
		function ParseFile(file) {
			if ("application/zip" != file.type) {
				$("#error")
						.css("display", "block")
						.html(
								"The file don't seem to be a zip. Do you want to continue?");
				$("#messages").css("display", "none");
				$("#success").css("display", "none");
			} else {
				$("#error").css("display", "none");
			}

			var m = $id("messages");
			m.innerHTML = "<p>File added: <strong>" + file.name + "</strong> ";
			$("#messages").css("display", "block");
			$("#success").css("display", "none");

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
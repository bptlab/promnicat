package de.uni_potsdam.hpi.bpt.promnicat.bpa;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.springframework.util.FileCopyUtils;

import de.uni_potsdam.hpi.bpt.ai.diagram.Diagram;
import de.uni_potsdam.hpi.bpt.ai.diagram.DiagramBuilder;
import de.uni_potsdam.hpi.bpt.ai.diagram.Shape;

public class JSONImporter {
	
	public static void main(String[] args) {
		final String path = "c:/Users/Marcin.Hewelt/workspace/promnicat/resources/BPMAI/model_bpmn0/1234/BPMN/2012-01-01_Test Model";
		final String modelName = "model_rev1.json";
		File folder = new File(path);
		if (folder.exists()) {
			File jsonModel = new File(folder,modelName);
			if (jsonModel.exists()) {
				String json;
				try {
					json = FileCopyUtils.copyToString(new InputStreamReader(new FileInputStream(jsonModel), "UTF-8"));
					Diagram diagram = DiagramBuilder.parseJson(json);
					System.out.println("Yuhu!\n"+diagram);
					for (Shape shape : diagram.getShapes()) {
						StringBuilder str = new StringBuilder();
						str.append("[");
						str.append(shape.getStencilId());
						str.append(":");
						str.append(shape.getProperty("name"));
						str.append("]->[");
						for (Shape sh : shape.getOutgoings()) {
							str.append(sh.getStencilId());
							str.append(",");
						}
						System.out.println(str.toString());
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else {
			System.out.println("Nein");
		}
		
	}
}

package GameUI;
/**
 * @author Kenneth Dale
 *
 */
import java.io.IOException;
import java.util.ArrayList;

import DataModels.Player;
import DataModels.Unit;
import UIFramework.NavigationController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

public class GameCreationController {
	
	@FXML Button teamMember1, teamMember2, teamMember3, battleButton; //Inject element IDs into controller
	
	@FXML HBox typeSelectContainer; //Inject element IDs into controller
	
	String teamMember; //Team member index
	int index;
	
	private int getIndex(String teamMember) {
		return (teamMember.equals("teamMember1")) ? 0 : (teamMember.equals("teamMember2")) ? 1 : 2;
	}
	
	@FXML
	private void handlePlayerSelect(ActionEvent event) throws IOException {
		teamMember = ((Button) event.getSource()).getId();
		index = getIndex(teamMember);
		typeSelectContainer.setVisible(true);
		
	}
	
	private void replaceButton(int index, String type) {
		if(index == 0) {
			teamMember1.getStyleClass().removeAll("unitSelect");
			if(type.equals("fire")) teamMember1.getStyleClass().add("fire");
			if(type.equals("earth")) teamMember1.getStyleClass().add("earth");
			if(type.equals("water")) teamMember1.getStyleClass().add("water");
		}
		else if(index == 1) {
			teamMember2.getStyleClass().removeAll("unitSelect");
			if(type.equals("fire")) teamMember2.getStyleClass().add("fire");
			if(type.equals("earth")) teamMember2.getStyleClass().add("earth");
			if(type.equals("water")) teamMember2.getStyleClass().add("water");
		}
		else {
			teamMember3.getStyleClass().removeAll("unitSelect");
			if(type.equals("fire")) teamMember3.getStyleClass().add("fire");
			if(type.equals("earth")) teamMember3.getStyleClass().add("earth");
			if(type.equals("water")) teamMember3.getStyleClass().add("water");
		}
	}
	
	@FXML
	private void handleTypeSelect(ActionEvent event) throws IOException {
		
		String type = ((Button) event.getSource()).getId();
		
		switch(type) {
			case "fire":
				Player.unitList.add(index, new Unit(type, 0, 10));
				typeSelectContainer.setVisible(false);
				replaceButton(index, type);
				break;
			case "earth":
				Player.unitList.add(index, new Unit(type, 0, 10));
				typeSelectContainer.setVisible(false);
				replaceButton(index, type);
				break;
			case "water":
				Player.unitList.add(index, new Unit(type, 0, 15));
				typeSelectContainer.setVisible(false);
				replaceButton(index, type);
				break;	
		}
		
		if(Player.unitList.size() == 3) {
			battleButton.setDisable(false);
		}
	}
	
	/** Send view change request */ 
	@FXML private void transitionView() {
		NavigationController.loadView(NavigationController.BATTLE);
	}
}

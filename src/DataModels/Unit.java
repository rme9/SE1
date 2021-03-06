package DataModels;

public class Unit {
	public String type;
	public int health;
	public int resource;
	public int damage; 
	
	public Unit(String newType, int newDamage, int newHealth, int newResource) {
		type = newType;
		damage = newDamage; 
		health = newHealth;
		resource = newResource;
	}
	
	public void setType(String newType) {
		type = newType; 
	}
	public void setHealth(int newHealth) {
		health = newHealth;
	}
	public void setResource(int newResource) {
		resource = newResource;
	}
	public void setDamage(int newDamage) {
		damage = newDamage;
	}
	
	public String getType() {
		return type;
	}
	public int getHealth() {
		return health;
	}
	public int getResource() {
		return resource;
	}
	public int getDamage() {
		return damage;
	}
	
	public void modHealth(int modBy) {
		health = health + modBy; 
	}
	
	public void modResource(int modBy) {
		resource = resource + modBy;
	}
}


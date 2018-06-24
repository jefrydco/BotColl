package id.jefrydco.botcoll.message.model;

public class BaseSender {
    private String name;
    private String profileUrl;

    BaseSender(String name, String profileUrl) {
        this.name = name;
        this.profileUrl = profileUrl;
    }

    public String getName() {
        return name;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }
}

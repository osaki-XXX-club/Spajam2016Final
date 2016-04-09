package jp.co.future.androidbase;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by mano on 2016/04/09.
 */
public class QiitaUser {

    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("facebook_id")
    @Expose
    private String facebookId;
    @SerializedName("followees_count")
    @Expose
    private Integer followeesCount;
    @SerializedName("followers_count")
    @Expose
    private Integer followersCount;
    @SerializedName("github_login_name")
    @Expose
    private String githubLoginName;
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("items_count")
    @Expose
    private Integer itemsCount;
    @SerializedName("linkedin_id")
    @Expose
    private String linkedinId;
    @SerializedName("location")
    @Expose
    private String location;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("organization")
    @Expose
    private String organization;
    @SerializedName("permanent_id")
    @Expose
    private Integer permanentId;
    @SerializedName("profile_image_url")
    @Expose
    private String profileImageUrl;
    @SerializedName("twitter_screen_name")
    @Expose
    private String twitterScreenName;
    @SerializedName("website_url")
    @Expose
    private String websiteUrl;

    /**
     * @return The description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description The description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return The facebookId
     */
    public String getFacebookId() {
        return facebookId;
    }

    /**
     * @param facebookId The facebook_id
     */
    public void setFacebookId(String facebookId) {
        this.facebookId = facebookId;
    }

    /**
     * @return The followeesCount
     */
    public Integer getFolloweesCount() {
        return followeesCount;
    }

    /**
     * @param followeesCount The followees_count
     */
    public void setFolloweesCount(Integer followeesCount) {
        this.followeesCount = followeesCount;
    }

    /**
     * @return The followersCount
     */
    public Integer getFollowersCount() {
        return followersCount;
    }

    /**
     * @param followersCount The followers_count
     */
    public void setFollowersCount(Integer followersCount) {
        this.followersCount = followersCount;
    }

    /**
     * @return The githubLoginName
     */
    public String getGithubLoginName() {
        return githubLoginName;
    }

    /**
     * @param githubLoginName The github_login_name
     */
    public void setGithubLoginName(String githubLoginName) {
        this.githubLoginName = githubLoginName;
    }

    /**
     * @return The id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id The id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return The itemsCount
     */
    public Integer getItemsCount() {
        return itemsCount;
    }

    /**
     * @param itemsCount The items_count
     */
    public void setItemsCount(Integer itemsCount) {
        this.itemsCount = itemsCount;
    }

    /**
     * @return The linkedinId
     */
    public String getLinkedinId() {
        return linkedinId;
    }

    /**
     * @param linkedinId The linkedin_id
     */
    public void setLinkedinId(String linkedinId) {
        this.linkedinId = linkedinId;
    }

    /**
     * @return The location
     */
    public String getLocation() {
        return location;
    }

    /**
     * @param location The location
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name The name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return The organization
     */
    public String getOrganization() {
        return organization;
    }

    /**
     * @param organization The organization
     */
    public void setOrganization(String organization) {
        this.organization = organization;
    }

    /**
     * @return The permanentId
     */
    public Integer getPermanentId() {
        return permanentId;
    }

    /**
     * @param permanentId The permanent_id
     */
    public void setPermanentId(Integer permanentId) {
        this.permanentId = permanentId;
    }

    /**
     * @return The profileImageUrl
     */
    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    /**
     * @param profileImageUrl The profile_image_url
     */
    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    /**
     * @return The twitterScreenName
     */
    public String getTwitterScreenName() {
        return twitterScreenName;
    }

    /**
     * @param twitterScreenName The twitter_screen_name
     */
    public void setTwitterScreenName(String twitterScreenName) {
        this.twitterScreenName = twitterScreenName;
    }

    /**
     * @return The websiteUrl
     */
    public String getWebsiteUrl() {
        return websiteUrl;
    }

    /**
     * @param websiteUrl The website_url
     */
    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

}
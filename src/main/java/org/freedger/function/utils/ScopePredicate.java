package org.freedger.function.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.regex.Pattern;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

public class ScopePredicate implements BiPredicate<Claim, DecodedJWT> {
    public static final String CLAIM_NAME = "scope";
    private final Pattern SPACE = Pattern.compile(" +");
    private final List<String> scopes;

    public ScopePredicate(String[] scopes) {
        this.scopes = Collections.unmodifiableList(Arrays.asList(scopes));
    }

    @Override
    public boolean test(Claim claim, DecodedJWT jwt) {
        String claimStr = claim.asString();
        if (claimStr == null) {
            return false;
        }
        
        Set<String> claimScopes = new HashSet<String>(Arrays.asList(SPACE.split(claimStr)));
        return scopes.stream().allMatch(claimScopes::contains);
    }
}
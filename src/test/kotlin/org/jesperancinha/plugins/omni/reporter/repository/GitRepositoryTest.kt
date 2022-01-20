package org.jesperancinha.plugins.omni.reporter.repository

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.Constants.HEAD
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.lib.RepositoryBuilder
import org.eclipse.jgit.revwalk.RevWalk
import org.junit.jupiter.api.Test
import java.io.File

internal class GitRepositoryTest {

    @Test
    fun `should get branch name`() {
        val resource = javaClass.getResource("/")
        resource.shouldNotBeNull()
        val repo = RepositoryBuilder().findGitDir(File(resource.toURI())).build()
        repo.branch.shouldNotBeNull()
        val exactRef = repo.exactRef("HEAD")
        exactRef.shouldNotBeNull()
        exactRef.target.name.shouldNotBeNull()
        val shortenRefName = Repository.shortenRefName(repo.fullBranch)
        shortenRefName.shouldNotBeNull()
        val revision = repo.resolve(HEAD)
        val commit = RevWalk(repo).parseCommit(revision)
        commit.shouldNotBeNull()
        val fullMessage = commit.fullMessage
        fullMessage.shouldNotBeNull()
        val id = commit.id.name
        id.shouldNotBeNull()
    }
}
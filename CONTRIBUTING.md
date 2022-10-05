# How to contribute to the project

This project welcomes any change, improvement, or suggestion!

If you'd like to help its development feel free to open a new issue and raise a pull request.

## IMPORTANT

If you'd like to work on an existing issue, kindly **ask** for it to be assigned to you.

Do you have any struggles with the issue you are working on? Feel free to **tag me** in it _and/or_ open a draft pull request.

## How do I make a contribution?

If you've never made an open source contribution before or are curious about how contributions operate in our project? Here's a quick rundown!

<img align="right" width="300" src="https://github.com/rob93c/Stickerify/blob/main/src/main/resources/fork.png" alt="fork this repository" />

#### If you don't have git on your machine, [install it](https://help.github.com/articles/set-up-git/).

## Fork this repository

Fork this repository by clicking on the fork button on the top of this page.
This will create a copy of this repository in your account `<your-GitHub-username>/<repository-name>`.

## Clone the repository

<img align="right" width="300" src="https://github.com/rob93c/Stickerify/blob/main/src/main/resources/clone.png" alt="clone this repository" />

Now clone the forked repository to your machine. Go to your GitHub account, open the forked repository, click on the code button and then click the _copy to clipboard_ icon.

Open a terminal and run the following git command:

```
git clone "url you just copied"
```

where "URL you just copied" (without quotation marks) is the URL to this repository (your fork of this project). 

<img align="right" width="300" src="https://github.com/rob93c/Stickerify/blob/main/src/main/resources/copy-to- clipboard.png" alt="copy URL to clipboard" />

For example:

```
git clone https://github.com/your-user-name/Stickerify.git
```

## Create a new branch for your changes or fix 

    ```sh
     $ git checkout -b <branch-name>
    ```

## Setup the project in your local by following the steps listed in the [README.md](https://github.com/rob93c/Stickerify#how-to-set-up-the-project) file.

6. Open the project in a code editor and begin working on it.
7. Add the contents of the changed files to the "snapshot" git uses to manage the state of the project, also known as the index

    ```sh
    $ git add .
    ```

8. Add a descriptive commit message

    ```sh
    $ git commit -m "Insert a short message of the changes made here"
    ```

9. Push the changes to the remote repository

    ```sh
    $ git push -u origin <branch-name>
    ```

10. Submit a pull request to the upstream repository.
